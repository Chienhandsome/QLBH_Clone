package kazuko.ex.report.bill;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter giới hạn cho PoC ngx-jrxml-editor. Chuyển compact element syntax của
 * JasperReports 7 về JRXML 6 cho text/field/image và các shape cơ bản.
 */
public final class Jrxml7To6Converter {
    private static final Set<String> SINGLE_BAND_SECTIONS = Set.of(
            "background", "title", "pageHeader", "columnHeader", "columnFooter",
            "pageFooter", "lastPageFooter", "summary", "noData");
    private static final Set<String> SUPPORTED_KINDS = Set.of(
            "staticText", "textField", "image", "line", "rectangle", "ellipse",
            "frame", "break", "elementGroup");

    private Jrxml7To6Converter() {}

    public static String convert(String source) throws Exception {
        if (source == null || source.isBlank()) throw new IllegalArgumentException("JRXML không được trống");
        Document document = parseSecurely(source);
        convertSingleBandSections(document);

        List<Element> compactElements = new ArrayList<>();
        NodeList all = document.getElementsByTagNameNS("*", "element");
        for (int i = 0; i < all.getLength(); i++) {
            Element element = (Element) all.item(i);
            if (element.hasAttribute("kind")) compactElements.add(element);
        }
        for (int i = compactElements.size() - 1; i >= 0; i--) {
            Element compact = compactElements.get(i);
            compact.getParentNode().replaceChild(convertElement(document, compact), compact);
        }
        normalizeContentElements(document);
        return write(document);
    }

    /**
     * Editor có thể đặt xuống dòng và khoảng trắng định dạng XML ở ngoài CDATA.
     * Jasper xem chúng là một phần nội dung; với text cao một dòng, dòng trắng
     * đầu tiên sẽ chiếm hết vùng in và làm phần chữ thực tế bị cắt mất.
     */
    private static void normalizeContentElements(Document document) {
        for (String name : List.of("text", "textFieldExpression", "imageExpression")) {
            NodeList nodes = document.getElementsByTagNameNS("*", name);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String content = element.getTextContent();
                if (content == null) continue;
                String normalized = content.strip();
                if (content.equals(normalized)) continue;
                while (element.hasChildNodes()) element.removeChild(element.getFirstChild());
                element.appendChild(document.createCDATASection(normalized));
            }
        }
    }

    private static Document parseSecurely(String source) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(source)));
    }

    private static void convertSingleBandSections(Document document) {
        Element root = document.getDocumentElement();
        for (Element section : childElements(root)) {
            String name = localName(section);
            if (!SINGLE_BAND_SECTIONS.contains(name) || !section.hasAttribute("height") ||
                    firstChild(section, "band") != null) continue;
            Element band = create(document, "band");
            copyAttribute(section, band, "height", "height");
            copyAttribute(section, band, "splitType", "splitType");
            List<Node> children = childNodes(section);
            for (Node child : children) band.appendChild(child);
            section.removeAttribute("height");
            section.removeAttribute("splitType");
            section.appendChild(band);
        }
    }

    private static Element convertElement(Document document, Element compact) {
        String kind = compact.getAttribute("kind");
        if (!SUPPORTED_KINDS.contains(kind)) {
            throw new IllegalArgumentException("Canvas đã dùng thành phần chưa hỗ trợ với JasperReports 6: " + kind);
        }
        if ("elementGroup".equals(kind)) {
            Element group = create(document, "elementGroup");
            moveRemainingChildren(compact, group);
            return group;
        }

        Element result = create(document, kind);
        copySpecificAttributes(compact, result, kind);
        Element reportElement = create(document, "reportElement");
        copyCommonAttributes(compact, reportElement);
        moveChildren(compact, reportElement, Set.of("property", "printWhenExpression"));
        result.appendChild(reportElement);

        Element box = detachFirst(compact, "box");
        if (box != null) result.appendChild(box);

        if ("staticText".equals(kind) || "textField".equals(kind)) {
            Element textElement = createTextElement(document, compact);
            if (textElement != null) result.appendChild(textElement);
        }

        if ("staticText".equals(kind)) {
            moveRenamedChild(document, compact, result, "text", "text");
        } else if ("textField".equals(kind)) {
            moveRenamedChild(document, compact, result, "expression", "textFieldExpression");
        } else if ("image".equals(kind)) {
            moveRenamedChild(document, compact, result, "expression", "imageExpression");
        }

        moveRemainingChildren(compact, result);
        return result;
    }

    private static void copySpecificAttributes(Element from, Element to, String kind) {
        switch (kind) {
            case "staticText" -> copyAttribute(from, to, "textAdjust", "textAdjust");
            case "textField" -> {
                copyAttribute(from, to, "pattern", "pattern");
                copyAttribute(from, to, "stretchWithOverflow", "isStretchWithOverflow");
                copyAttribute(from, to, "textAdjust", "textAdjust");
                copyAttribute(from, to, "blankWhenNull", "isBlankWhenNull");
                copyAttribute(from, to, "evaluationTime", "evaluationTime");
                copyAttribute(from, to, "evaluationGroup", "evaluationGroup");
                copyHyperlinkAttributes(from, to);
            }
            case "image" -> {
                copyAttribute(from, to, "scaleImage", "scaleImage");
                copyAttribute(from, to, "hImageAlign", "hAlign");
                copyAttribute(from, to, "vImageAlign", "vAlign");
                copyAttribute(from, to, "usingCache", "isUsingCache");
                copyAttribute(from, to, "lazy", "isLazy");
                copyAttribute(from, to, "onErrorType", "onErrorType");
                copyHyperlinkAttributes(from, to);
            }
            case "line" -> copyAttribute(from, to, "direction", "direction");
            case "rectangle" -> copyAttribute(from, to, "radius", "radius");
            case "break" -> copyAttribute(from, to, "type", "type");
            default -> { }
        }
    }

    private static void copyHyperlinkAttributes(Element from, Element to) {
        copyAttribute(from, to, "hyperlinkType", "hyperlinkType");
        copyAttribute(from, to, "hyperlinkTarget", "hyperlinkTarget");
    }

    private static void copyCommonAttributes(Element from, Element to) {
        for (String name : List.of("key", "style", "positionType", "stretchType", "mode",
                "x", "y", "width", "height", "forecolor", "backcolor",
                "printWhenGroupChanges", "uuid")) {
            copyAttribute(from, to, name, name);
        }
        copyAttribute(from, to, "printRepeatedValues", "isPrintRepeatedValues");
        copyAttribute(from, to, "removeLineWhenBlank", "isRemoveLineWhenBlank");
    }

    private static Element createTextElement(Document document, Element compact) {
        boolean hasTextSettings = false;
        Element textElement = create(document, "textElement");
        for (String[] mapping : List.of(
                new String[]{"hTextAlign", "textAlignment"},
                new String[]{"vTextAlign", "verticalAlignment"},
                new String[]{"rotation", "rotation"},
                new String[]{"markup", "markup"})) {
            if (compact.hasAttribute(mapping[0])) {
                textElement.setAttribute(mapping[1], compact.getAttribute(mapping[0]));
                hasTextSettings = true;
            }
        }
        Element paragraph = detachFirst(compact, "paragraph");
        if (paragraph != null) {
            textElement.appendChild(paragraph);
            hasTextSettings = true;
        }
        Element font = create(document, "font");
        for (String[] mapping : List.of(
                new String[]{"fontName", "fontName"}, new String[]{"fontSize", "size"},
                new String[]{"bold", "isBold"}, new String[]{"italic", "isItalic"},
                new String[]{"underline", "isUnderline"}, new String[]{"strikeThrough", "isStrikeThrough"},
                new String[]{"pdfFontName", "pdfFontName"}, new String[]{"pdfEncoding", "pdfEncoding"},
                new String[]{"pdfEmbedded", "isPdfEmbedded"})) {
            if (compact.hasAttribute(mapping[0])) {
                font.setAttribute(mapping[1], compact.getAttribute(mapping[0]));
                hasTextSettings = true;
            }
        }
        if (font.hasAttributes()) textElement.appendChild(font);
        return hasTextSettings ? textElement : null;
    }

    private static void moveChildren(Element from, Element to, Set<String> names) {
        for (Element child : childElements(from)) {
            if (names.contains(localName(child))) to.appendChild(child);
        }
    }

    private static void moveRenamedChild(Document document, Element from, Element to,
                                         String oldName, String newName) {
        Element child = detachFirst(from, oldName);
        if (child == null) return;
        Element renamed = create(document, newName);
        while (child.hasChildNodes()) renamed.appendChild(child.getFirstChild());
        to.appendChild(renamed);
    }

    private static void moveRemainingChildren(Element from, Element to) {
        for (Node child : childNodes(from)) {
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().isBlank()) continue;
            to.appendChild(child);
        }
    }

    private static Element detachFirst(Element parent, String name) {
        Element child = firstChild(parent, name);
        if (child != null) parent.removeChild(child);
        return child;
    }

    private static Element firstChild(Element parent, String name) {
        for (Element child : childElements(parent)) {
            if (name.equals(localName(child))) return child;
        }
        return null;
    }

    private static List<Element> childElements(Element parent) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element element) result.add(element);
        }
        return result;
    }

    private static List<Node> childNodes(Element parent) {
        List<Node> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) result.add(children.item(i));
        return result;
    }

    private static Element create(Document document, String name) {
        return document.createElementNS(document.getDocumentElement().getNamespaceURI(), name);
    }

    private static String localName(Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    }

    private static void copyAttribute(Element from, Element to, String source, String target) {
        if (from.hasAttribute(source)) to.setAttribute(target, from.getAttribute(source));
    }

    private static String write(Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        var transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}

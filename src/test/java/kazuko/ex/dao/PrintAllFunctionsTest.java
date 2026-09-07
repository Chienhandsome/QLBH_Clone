package kazuko.ex.dao;

import kazuko.ex.model.system.SFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PrintAllFunctionsTest {
    @Test
    public void printFunctions() {
        FunctionDAO dao = new FunctionDAO();
        List<SFunction> list = dao.getAllFunctions();
        for (SFunction f : list) {
            System.out.printf("Group: %-15s | ID: %-36s | Name: %-35s | Note: %s%n",
                    f.getGroupName(), f.getId(), f.getName(), f.getNote());
        }
    }
}

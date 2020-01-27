package unicauca.front.end.service;
import java.util.HashMap;

public class Consulta {

    private HashMap<String, Object> selector;

    public Consulta() {
        selector=new HashMap<>();
    }
   

    public HashMap<String, Object> getSelector() {
        return selector;
    }

    public void setSelector(HashMap<String, Object> selector) {
        this.selector = selector;
    }

    public void searchRol(String rol){
        HashMap<String, Object> values = new HashMap<>();
        values.put("$eq", rol); 
        ElemMatch elem=new ElemMatch(values);
        selector.put("roles", elem);
    }
    
    public void searchPersona(String array,String campo, String valor){
        HashMap<String, Object> values = new HashMap<>();
        values.put(campo, new Eq(valor));        
        ElemMatch elem=new ElemMatch(values);
        selector.put(array, elem);
    }
    
    public void searchNormal(String campo,String valor){
        selector.put(campo, valor);
    }
}

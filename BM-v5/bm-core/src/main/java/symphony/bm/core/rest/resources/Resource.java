package symphony.bm.core.rest.resources;

import symphony.bm.core.rest.forms.Form;

public interface Resource {
    void create();
    boolean update(Form form) throws Exception;
//    boolean updateField(String fieldName, Object fieldValue) throws Exception;
    void delete();
}

package symphony.bm.core.rest.interfaces;

import symphony.bm.core.rest.forms.Form;

import java.util.Map;

public interface Resource {
    void create();
    boolean update(Form form);
    void delete();
}

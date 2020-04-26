package symphony.bm.core.rest.resources;

import symphony.bm.core.rest.forms.Form;

public interface Resource {
    void create();
    boolean update(Form form);
    void delete();
}

package symphony.bm.core.rest.resources;

import symphony.bm.core.rest.forms.Form;

public interface Resource {
    boolean update(Form form) throws Exception;
}

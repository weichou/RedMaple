package redmaple.menu.ui;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 21.3.2013
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class FilteredList extends List {
    private Object[] rawItems;
    private Object[] originalItems;

    public FilteredList(Object[] items, Skin skin) {
        super(items, skin);
        rawItems = items;
        originalItems = items;
    }

    public void updateFilter(ListFilter filter) {
        if (filter == null) {
            setItems(originalItems);
        }
        else {
            Array<Object> filteredObjects = new Array<Object>(originalItems.length/2); // Kinda rough estimation
            for (Object object : originalItems) {
                if (filter.accept(object, filter.argument))
                    filteredObjects.add(object);
            }
            setItems(filteredObjects.toArray(Object.class));
        }

    }

    @Override
    public void setItems(Object[] objects) {
        super.setItems(objects);
        this.rawItems = objects;
    }

    public Object getSelectedObject() {
        int idx = getSelectedIndex();
        if (idx < 0)
            return null;
        return this.rawItems[idx];
    }

    public int getObjectsLength() {
        return this.rawItems.length;
    }

    public static abstract class ListFilter {
        public String argument;

        public ListFilter(String argument) {
            this.argument = argument;
        }

        public abstract boolean accept(Object o, String argument);
    }

}

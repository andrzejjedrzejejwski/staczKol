package aj.software.staczKolejkowy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import static aj.software.staczKolejkowy.core.Department.Result.Group;

public class StandsAdapter extends ArrayAdapter<Group> {

    private Context context;
    private List<Group> items;

    public StandsAdapter(Context context, List<Group> groups) {
        super(context, android.R.layout.simple_list_item_1, groups);
        this.context = context;
        this.items = groups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.findViewsIn(convertView);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.update(items.get(position));
        return convertView;
    }

    class ViewHolder{
        private TextView tv;

        public void update(Group group) {
            tv.setText(group.getName());
        }

        public void findViewsIn(View convertView) {
            tv = (TextView) convertView.findViewById(android.R.id.text1);
        }
    }
}

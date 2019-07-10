package moeo.moeo.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import moeo.moeo.R;

public class CustomDiaryAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<DiaryItem> mItems = new ArrayList<>();
    private int tvColor;

    public CustomDiaryAdapter(ArrayList<DiaryItem> mItem,int tvColor){
        this.mItems = mItem;
        this.tvColor = tvColor;
    }
    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public DiaryItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customdiary, parent, false);
        }
        if(position>=mItems.size()){
            return convertView;
        }
        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        TextView tv_contents = (TextView) convertView.findViewById(R.id.tv_contents) ;
        TextView tv_count = (TextView) convertView.findViewById(R.id.tv_count) ;

        /* 각 리스트에 뿌려줄 아이템을 받아오는데 mMyItem 재활용 */
        DiaryItem myItem = getItem(position);

        /* 각 위젯에 세팅된 아이템을 뿌려준다 */
        tv_contents.setText(myItem.getItem());
        tv_contents.setTextColor(this.tvColor);
        tv_count.setText(myItem.getCount());

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */


        return convertView;
    }
}
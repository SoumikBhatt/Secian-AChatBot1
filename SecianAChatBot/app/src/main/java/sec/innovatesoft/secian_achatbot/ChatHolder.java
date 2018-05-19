package sec.innovatesoft.secian_achatbot;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatHolder extends RecyclerView.ViewHolder  {

    TextView leftText,rightText;

    public ChatHolder(View itemView){
        super(itemView);

        leftText = itemView.findViewById(R.id.leftText);
        rightText = itemView.findViewById(R.id.rightText);


    }
}

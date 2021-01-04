package com.simpelexo.alyfas5anyserver.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.async.AsyncEscPosPrinter;
import com.simpelexo.alyfas5anyserver.async.AsyncTcpEscPosPrint;
import com.simpelexo.alyfas5anyserver.callback.ItemClickListener;
import com.simpelexo.alyfas5anyserver.model.CartItem;
import com.simpelexo.alyfas5anyserver.model.OrderModel;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    Context context;
    List<OrderModel> orderModelList;
    SimpleDateFormat simpleDateFormat;
    OrderDetailAdapter orderDetailAdapter;
    private String Bill="" ;
    private EscPosCharsetEncoding escPosCharsetEncoding;
    private List<OrderModel> orderModel;
    ;

    @SuppressLint("SimpleDateFormat")
    public OrderAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        //simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ", Locale.ENGLISH);
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(orderModelList.get(position).getCartItemList().get(0).getFoodImage()).into(holder.img_food_image);
        holder.txt_order_number.setText(orderModelList.get(position).getKey());
        Common.setSpanStringColor(context.getString(R.string.order_date),simpleDateFormat.format(orderModelList.get(position).getCreateDate()),
                holder.txt_time, Color.parseColor("#333639"));
     Common.setSpanStringColor(context.getString(R.string.order_status),Common.convertCodeToStatus(orderModelList.get(position).getOrderStatus()),
                holder.txt_order_status, Color.parseColor("#00579A"));
     Common.setSpanStringColor(context.getString(R.string.name),orderModelList.get(position).getUserName(),
                holder.txt_name, Color.parseColor("#00574B"));
     Common.setSpanStringColor(context.getString(R.string.item_num),orderModelList.get(position).getCartItemList()== null ? "0":
             String.valueOf(orderModelList.get(position).getCartItemList().size()),
                holder.txt_num_item, Color.parseColor("#4B647D"));
        Common.setSpanStringColor(context.getString(R.string.total_price), String.valueOf(orderModelList.get(position).getTotalPayment()),
                holder.txt_total_payment, Color.parseColor("#00574B"));
        Common.setSpanStringColor(context.getString(R.string.comment), String.valueOf(orderModelList.get(position).getComment()),
                holder.txt_comment, Color.parseColor("#00574B"));
        Common.setSpanStringColor(context.getString(R.string.address), String.valueOf(orderModelList.get(position).getShippingAddress()),
                holder.txt_address, Color.parseColor("#00574B"));

//       Double food_quantit_n = orderModelList.get(position).getCartItemList().get(position).getFoodQuantity();
//       Double food_price_n = orderModelList.get(position).getCartItemList().get(position).getFoodPrice();
       //Double tot = food_price_n*food_quantit_n;
//        Common.setSpanStringColor(context.getString(R.string.total_price), String.valueOf(tot),
//                holder.txt_total_payment, Color.parseColor("#00574B"));

     holder.setItemClickListener((view, position1, isLongClick) -> {
             Common.selectedOrder = orderModelList.get(position1);
       // showDialog(orderModelList.get(position1).getCartItemList()));
        showDialog(Common.selectedOrder.getCartItemList());
     });
     
    }

//    private StringBuilder billList(){
//        for (int i = 0; i < Common.selectedOrder.getCartItemList().size(); i++)
//        {
//            try {
//                //  Bill = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName();
//                Bill = new StringBuilder();
//
//                String  foodName ="[R]"+Common.selectedOrder.getCartItemList().get(i).getFoodName() ;
//                String foodPrice = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodPrice().toString();
//                Bill.append(foodName);
//
//                Bill.append(foodPrice);
//                Bill.append("ج.م");
//
////                                                    Bill.append("\n  Date  : " + Common.selectedOrder.getCartItemList().get(i).getUserPhone());
////                                                    Bill.append("\n  Time  : " + Common.selectedOrder.getCartItemList().get(i).getFoodPrice());
//                //   Bill.append("\n  Venue : " + Common.selectedOrder.getCartItemList().get(i).getVenue());
//                Bill.append("\n");
//
//            return Bill;
//            }catch (Exception e){e.printStackTrace();}
//
//        }
//        return Bill;
//    }
    private void showDialog(List<CartItem> cartItemList) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout_dialog);
        Button btn_ok =(Button)layout_dialog.findViewById(R.id.btn_ok);
        Button btn_print =(Button)layout_dialog.findViewById(R.id.btn_print);
        RecyclerView rv_order_detail = (RecyclerView)layout_dialog.findViewById(R.id.rv_order_detail);
        rv_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rv_order_detail.setLayoutManager(layoutManager);
        rv_order_detail.addItemDecoration(new DividerItemDecoration(context,layoutManager.getOrientation()));
        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(context,cartItemList);
        rv_order_detail.setAdapter(orderDetailAdapter);
        //show
        AlertDialog dialog = builder.create();
        dialog.show();
        //show keyboard problem
        dialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);


        btn_ok.setOnClickListener(v -> {
           dialog.dismiss();
            notifyDataSetChanged();



        });
        btn_print.setOnClickListener(v -> {
            //orderDetailAdapter.grandTotal(cartItemList);
            Bill = "";

        printTcp();

//            new Thread(() -> {
//                try {
//
//                    EscPosPrinter printer = new EscPosPrinter(new TcpConnection("192.168.1.50", 9100), 203,
//                            80f, 48
//                            ,new EscPosCharsetEncoding("Windows-1256",33));
//                    for (int i = 0; i < Common.selectedOrder.getCartItemList().size(); i++)
//                    {
//                        try {
//                            //  Bill = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName();
//                          //  Bill = new StringBuilder();
//
//                            String  foodName ="[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName() ;
//                            String foodQuant = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodQuantity().toString()+" ك ";
//                            String foodPrice = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodPrice().toString()+" ج.م ";
////                                Bill.append(foodName)
//                        Bill += foodName+foodQuant+foodPrice+"\n";
//
//
////                                                    Bill.append("\n  Date  : " + Common.selectedOrder.getCartItemList().get(i).getUserPhone());
////                                                    Bill.append("\n  Time  : " + Common.selectedOrder.getCartItemList().get(i).getFoodPrice());
//                            //   Bill.append("\n  Venue : " + Common.selectedOrder.getCartItemList().get(i).getVenue());
////                                Bill.append("\n");
//
//
//                        }catch (Exception e){e.printStackTrace();}
//
//                    }
//                    Drawable drawable = context.getResources().getDrawableForDensity(R.drawable.aly_logo, DisplayMetrics.DENSITY_220);
//                                           /* "[L]\n" +
//                                            "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
//                                            "[L]  + Size : S\n" +
//                                            "[L]\n" +
//                                            "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
//                                            "[L]  + Size : 57/58\n" +
//                                            "[L]\n" +
//                                            "[C]--------------------------------\n" +
//                                            "[R]TOTAL PRICE :[R]34.98e\n" +
//                                            "[R]TAX :[R]4.23e\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<font size='tall'>Customer :</font>\n" +
//                                            "[L]Raymond DUPONT\n" +
//                                            "[L]5 rue des girafes\n" +
//                                            "[L]31547 PERPETES\n" +
//                                            "[L]Tel : +33801201456\n" +
//                                            "[L]\n" +
//                                            "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                                            "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"*/
//
//                    printer
//                            .printFormattedTextAndCut(
//                                    "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, drawable) + "</img>\n" +
//                                            "[L]\n" +
//                                            "[L]<u><font size='big'>طلب رقم :"+"</font></u>"+Common.selectedOrder.getKey()+"\n" +
//                                            "[C]<b>\n"+simpleDateFormat.format(Common.selectedOrder.getCreateDate())+"<b>"+
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[R]\n"+
//                                               Bill+
//                                            "[L]\n" +
//                                            "[C]--------------------------------\n" +
//                                            "[L]الاجمالي :" +"[L]"+Common.selectedOrder.getTotalPayment()+" ج.م "+"\n"+
////                                                "[R]TAX :[R]4.23e\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<font size='tall'>عميل :</font>\n" +
//                                            "[L] الأسم :"+Common.selectedOrder.getUserName()+"\n"+
//                                            "[L] العنوان :"+Common.selectedOrder.getShippingAddress()+"\n"+
//                                            "[L]"+"تليفون :"+"R"+Common.selectedOrder.getUserPhone()+"\n"+
//                                            "[L]\n" +
////                                                "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                                            "[C]<qrcode size='20'>http://https://www.facebook.com/FASAKHANYALYELFASAKHNY/</qrcode>"+"\n"
//                                    +"[L]<b>Simplexo Programming Solution</b>[R]01149638073 \n"
//
//
//                                    ,25f);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
        });
    }
     /*==============================================================================================
    =========================================TCP PART===============================================
    ==============================================================================================*/

    public void printTcp() {


        try {
//             this.printIt(new TcpConnection("192.168.1.50", 9100));
            new AsyncTcpEscPosPrint(context)

             .execute(this.getAsyncEscPosPrinter(new TcpConnection("192.168.1.50", 9100)));
            // .execute(this.printIt(new TcpConnection("192.168.1.50",9100)));
        } catch (NumberFormatException e) {

            e.printStackTrace();
        }
    }

//    public void printIt(DeviceConnection printerConnection) {
//        try {
//            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 80f, 48,new EscPosCharsetEncoding("Windows-1256",33));
//            // printer= new AsyncEscPosPrinter();
//            Drawable drawable = context.getResources().getDrawableForDensity(R.drawable.aly_logo, DisplayMetrics.DENSITY_220);
//            for (int i = 0; i < Common.selectedOrder.getCartItemList().size(); i++)
//            {
//                try {
//                    //  Bill = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName();
//                    //  Bill = new StringBuilder();
//
//                    String  foodName ="[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName() ;
//                    String foodQuant = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodQuantity().toString()+" ك ";
//                    String foodPrice = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodPrice().toString()+" ج.م ";
////                                Bill.append(foodName)
//                    Bill += foodName+foodQuant+foodPrice+"\n";
//
//                }catch (Exception e){e.printStackTrace();}}
//            printer
//                    .printFormattedText(
//                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, drawable) + "</img>\n" +
//                                    "[L]\n" +
//                                    "[L]<u><font size='big'>طلب رقم :"+"</font></u>"+Common.selectedOrder.getKey()+"\n" +
//                                    "[C]<b>\n"+simpleDateFormat.format(Common.selectedOrder.getCreateDate())+"<b>"+
//                                    "[L]\n" +
//                                    "[C]================================\n" +
//                                    "[R]\n"+
//                                    Bill+
//                                    "[L]\n" +
//                                    "[C]--------------------------------\n" +
//                                    "[L]الاجمالي :" +"[L]"+Common.selectedOrder.getTotalPayment()+" ج.م "+"\n"+
////                                                "[R]TAX :[R]4.23e\n" +
//                                    "[L]\n" +
//                                    "[C]================================\n" +
//                                    "[L]\n" +
//                                    "[L]<font size='tall'>عميل :</font>\n" +
//                                    "[L] الأسم :"+Common.selectedOrder.getUserName()+"\n"+
//                                    "[L] العنوان :"+Common.selectedOrder.getShippingAddress()+"\n"+
//                                    "[L]"+"تليفون :"+"R"+Common.selectedOrder.getUserPhone()+"\n"+
//                                    "[L]\n" +
////                                                "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                                    "[C]<qrcode size='20'>http://https://www.facebook.com/FASAKHANYALYELFASAKHNY/</qrcode>"+"\n"
//                                    +"[L]<b>Simplexo Programming Solution</b>[R]01149638073 \n"
//
//
//                            ,30f);
//
//
//    }catch (Exception e){}}

    /**
     * Asynchronous printing
     */
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
//        escPosCharsetEncoding=new EscPosCharsetEncoding("Windows-1256",33);
        //AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 80f, 48);
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 80f, 48);




       // printer= new AsyncEscPosPrinter();
        Drawable drawable = context.getResources().getDrawableForDensity(R.drawable.aly_logo, DisplayMetrics.DENSITY_220);
        for (int i = 0; i < Common.selectedOrder.getCartItemList().size(); i++)
        {
            try {
                //  Bill = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName();
                //  Bill = new StringBuilder();

                String  foodName ="[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodName() ;
                String foodQuant = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodQuantity().toString()+" ك ";
                String foodPrice = "[L]"+Common.selectedOrder.getCartItemList().get(i).getFoodPrice().toString()+" ج.م ";
//                                Bill.append(foodName)
                Bill += foodName+foodQuant+foodPrice+"\n";

            }catch (Exception e){e.printStackTrace();}}
        return printer.setTextToPrint(
                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, drawable) + "</img>\n" +
                        "[L]\n" +
                        "[L]<u><font size='big'>طلب رقم :"+"</font></u>"+Common.selectedOrder.getKey()+"\n" +
                        "[C]<b>\n"+simpleDateFormat.format(Common.selectedOrder.getCreateDate())+"<b>"+
                        "[L]\n" +
                        "[C]================================\n" +
                        "[R]\n"+
                        Bill+
                        "[L]\n" +
                        "[C]--------------------------------\n" +
                        "[L]الاجمالي :" +"[L]"+Common.selectedOrder.getTotalPayment()+" ج.م "+"\n"+
//                                                "[R]TAX :[R]4.23e\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n" +
                        "[L]<font size='tall'>عميل :</font>\n" +
                        "[L] الأسم :"+Common.selectedOrder.getUserName()+"\n"+
                        "[L] العنوان :"+Common.selectedOrder.getShippingAddress()+"\n"+
                        "[L]"+"تليفون :"+"R"+Common.selectedOrder.getUserPhone()+"\n"+
                        "[L]\n" +
//                                                "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                        "[C]<qrcode size='20'>http://https://www.facebook.com/FASAKHANYALYELFASAKHNY/</qrcode>"+
                        "\n\n"



                );
        }



    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public OrderModel getItemPosition(int pos) {
    return orderModelList.get(pos);
    }

    public void removeItem(int pos) {
        orderModelList.remove(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_time)
        TextView txt_time;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_num_item)
        TextView txt_num_item;
        @BindView(R.id.txt_total_payment)
        TextView txt_total_payment;
        @BindView(R.id.txt_address)
        TextView txt_address;
        @BindView(R.id.txt_comment)
        TextView txt_comment;



        private Unbinder unbinder;

        ItemClickListener itemClickListener;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
          itemClickListener.onClick(v,getAdapterPosition(),false);
        }
    }
}

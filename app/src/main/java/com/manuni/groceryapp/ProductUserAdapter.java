package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manuni.groceryapp.databinding.DialogQuantityBinding;
import com.manuni.groceryapp.databinding.SampleProductUserBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ProductUserAdapter extends RecyclerView.Adapter<ProductUserAdapter.ProductUserViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> list,filterList;
    private FilterProductUser filterProductUser;
    private  String price;

    public ProductUserAdapter(Context context,ArrayList<ModelProduct> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public ProductUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_product_user,parent,false);
        return new ProductUserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProductUserViewHolder holder, int position) {
        ModelProduct data = list.get(position);

        String discountAvailable = data.getProductDiscountAvailable();
        String discountNote = data.getProductDiscountNote();
        String discountPrice = data.getProductDiscountPrice();
        String productCategory = data.getProductCategory();
        String originalPrice = data.getProductOriginalPrice();
        String productDescription = data.getProductDesc();
        String productTitle = data.getProductTitle();
        String productQuantity = data.getProductQuantity();
        String productId = data.getProductId();
        String timestamp = data.getTimestamp();
        String productIcon = data.getProductIcon();
        String productAvailable = data.getProductAvailable();
        String proBrand = data.getProductBrand();


        try {
            holder.binding.titleTV.setText(productTitle);
            holder.binding.descriptionTV.setText(productDescription);
            holder.binding.quantityTV.setText("পরিমাণঃ "+"("+productQuantity+")");

            holder.binding.originalPriceTV.setText("৳"+originalPrice);
            holder.binding.discountPriceTV.setText("৳"+discountPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (productAvailable.equals("false")&&(discountAvailable.equals("true")||discountAvailable.equals("false"))){
            try {
                holder.binding.productAvailableTV.setText("Not Available");
                holder.binding.productAvailableTV.setTextColor(context.getResources().getColor(R.color.colorRed));
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.productAvailableTV.setVisibility(View.VISIBLE);
                holder.binding.addToCartTV.setVisibility(View.INVISIBLE);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }

        }else {
            try {
                holder.binding.discountNoteTV.setText(discountNote+"% OFF");
                holder.binding.productAvailableTV.setVisibility(View.GONE);
                holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                holder.binding.addToCartTV.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (discountAvailable.equals("true") && productAvailable.equals("true")){
            try {
                holder.binding.discountPriceTV.setVisibility(View.VISIBLE);
                holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                holder.binding.originalPriceTV.setPaintFlags(holder.binding.discountPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                holder.binding.discountPriceTV.setVisibility(View.GONE);
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.originalPriceTV.setPaintFlags(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.impl1).into(holder.binding.productIconIV);
        }catch (Exception e){
            holder.binding.productIconIV.setImageResource(R.drawable.impl1);
        }

        try {
            holder.binding.addToCartTV.setOnClickListener(view -> showQuantityDialog(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,ProductInfoActivity.class);
            intent.putExtra("productIcon",productIcon);
            intent.putExtra("originalPrice",originalPrice);
            intent.putExtra("discountPrice",discountPrice);
            intent.putExtra("productTitle",productTitle);
            intent.putExtra("discountNote",discountNote);
            intent.putExtra("productDes",productDescription);
            intent.putExtra("productBrand",proBrand);
            intent.putExtra("priceEach",price);
            intent.putExtra("productId",data.getProductId());
            intent.putExtra("productQuantity",data.getProductQuantity());
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private double cost = 0.0,finalCost = 0.0,myFinalCost=0.0;
    private int quantity = 0;
    private String productQuantity;
    private String image;
    private  int number = 0;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showQuantityDialog(ModelProduct modelProduct) {
        DialogQuantityBinding binding;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        binding = DialogQuantityBinding.bind(view);

        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDesc();
        String discountNote = modelProduct.getProductDiscountNote();
        image = modelProduct.getProductIcon();


        if (modelProduct.getProductDiscountAvailable().equals("true")){
            price = modelProduct.getProductDiscountPrice();
            try {
                binding.discountNoteTV.setVisibility(View.VISIBLE);
                binding.originalPriceTV.setPaintFlags(binding.originalPriceTV.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            price = modelProduct.getProductOriginalPrice();
            try {
                binding.discountNoteTV.setVisibility(View.GONE);
                binding.discountPriceTV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            cost = Double.parseDouble(price.replaceAll("৳",""));
            finalCost = Double.parseDouble(price.replaceAll("৳",""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());

        String[] items = new String[]{"Pick","1","2","3","4","5","6","7","8", "9", "10", "11","12","13","14","15","16","17","18",
        "19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44",
                "45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67",
        "68","69","70","71","72","73","74","75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90",
        "91","92","93","94","95","96","97","98","99","100","101","102","103","104","105","106","107","108","109","110",
        "111","112","113","114","115","116","117","118","119","120","121","122","123","124","125","126","127","128","129","130",
        "131","132","133","134","135","136","137","138","139","140","141","142","143","144","145","146","147","148","149","150",
        "151","152","153","154","155","156","157","158","159","160","161","162","163","164","165","166","167","168","169","170",
        "171","172","173","174","175","176","177","178","179","180","181","182","183","184","185","186","187","188","189","190",
        "191","192","193","194","195","196","197","198","199","200","201","202","203","204","205","206","207","208","209","210",
        "211","212","213","214","215","216","217","218","219","220","221","222","223","224","225","226","227","228","229","230",
        "231","232","233","234","235","236","237", "238","239","240","241","242","243","244","245","246","247","248","249","250",
        "251","252","253","254","255","256","257","258","259","260","261","262","263","264","265","266","267","268","269","270",
        "271","272","273","274","275","276","277","278","279","280","281","282","283","284","285","286","287","288","289","290",
        "291","292","293","294","295","296","297","298","299","300","301","302","303","304","305","306","307","308","309","310",
        "311","312","313","314","315","316","317","318","319","320","321","322","323","324","325","326","327","328","329","330",
        "331","332","333","334","335","336","337","338","339","340","341","342","343","344","345","346","347","348","349","350",
        "351","352","353","354","355","356","357","358","359","360","361","362","363","364","365","366","367","368","369","370"};
        binding.quantitySpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.color_spinner_layout,items));

        binding.quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              String  selectQuantity = binding.quantitySpinner.getSelectedItem().toString();
              if (!selectQuantity.equals("Pick")){
                   number = Integer.parseInt(selectQuantity);
                  quantity = number;
                  binding.quantityTV.setText(""+quantity);

                  double priceDouble = Double.parseDouble(price);

                  myFinalCost = priceDouble*quantity;


                  binding.finalTV.setText("৳"+myFinalCost);



                  myFinalCost = priceDouble;

                  quantity = number;
                  Toast.makeText(context, ""+quantity, Toast.LENGTH_SHORT).show();


              }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(binding.productIV);
        }catch (Exception e){
            binding.productIV.setImageResource(R.drawable.ic_cart_gray);
        }
        try {
            binding.titleTV.setText(""+title);
            binding.quantityTV.setText(""+quantity);
            binding.pDescription.setText(""+description);
            binding.finalTV.setText("৳"+finalCost);
            binding.discountNoteTV.setText(""+discountNote+"% OFF");
            binding.originalPriceTV.setText("৳"+modelProduct.getProductOriginalPrice());
            binding.discountPriceTV.setText("৳"+modelProduct.getProductDiscountPrice());
            binding.pQuantityTV.setText("["+productQuantity+"]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        binding.incrementBtn.setOnClickListener(view13 -> {

            double myCost = Double.parseDouble(price);

            if (myFinalCost !=0){
                quantity++;
                myFinalCost = (myCost*quantity);
                finalCost = myFinalCost;

            }else {
                finalCost = finalCost+cost;
                quantity++;
            }






            Toast.makeText(context, ""+quantity, Toast.LENGTH_SHORT).show();

            try {
                binding.finalTV.setText("৳"+String.format("%.1f",finalCost));
                binding.quantityTV.setText(""+quantity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.decrementBtn.setOnClickListener(view12 -> {
            double myCost = Double.parseDouble(price);

            if (myFinalCost != 0){
                if (quantity > 1){
                    quantity--;
                    myFinalCost = (myCost*quantity);
                    finalCost = myFinalCost;
                }

            }else {
                if (quantity > 1) {
                    finalCost = finalCost - cost;
                    quantity--;
                }
            }

                    try {
                        binding.finalTV.setText("৳"+String.format("%.1f",finalCost));
                        binding.quantityTV.setText(""+quantity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



        });
        binding.continueBtn.setOnClickListener(view1 -> {
            String title1 = binding.titleTV.getText().toString().trim();
            String priceEach = price;
            String totalPrice = binding.finalTV.getText().toString().trim().replace("৳","");
            String quantity = binding.quantityTV.getText().toString().trim();
            String pQuantity = binding.pQuantityTV.getText().toString().trim();


            //add to database(sqlite)
            addToCart(productId, title1,priceEach,totalPrice,quantity,pQuantity,image);
            dialog.dismiss();
        });
    }



    private void addToCart(String productId, String title, String priceEach, String price, String quantity,String proQuantity,String prImage) {

      long myItem =  System.currentTimeMillis();


            myItem++;

       // Toast.makeText(context, ""+myItem, Toast.LENGTH_SHORT).show();

        /*
        EasyDB easyDB = EasyDB.init(context,"ITEM_DB_NEW")
                .setTableName("ITEM_TABLE_NEW")
                .addColumn(new Column("Items_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Items_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Each_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Quantity",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Quantity",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Image",new String[]{"text","not null"}))
                .doneTableColumn();*/


        EasyDB easyDB = EasyDB.init(context,"ITEM_DB_NEW_TWO")
                .setTableName("ITEM_TABLE_NEW_TWO")
                .addColumn(new Column("Items_Id_Two",new String[]{"text","unique"}))
                .addColumn(new Column("Items_PID_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Name_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Each_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Image_Two",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Items_Id_Two", (int) myItem)
                .addData("Items_PID_Two",productId)
                .addData("Items_Name_Two",title)
                .addData("Items_Each_Price_Two",priceEach)
                .addData("Items_Price_Two",price)
                .addData("Items_Quantity_Two",quantity)
                .addData("Items_Pro_Quantity_Two",proQuantity)
                .addData("Items_Pro_Image_Two",prImage)
                .doneDataAdding();

        Toast.makeText(context, "Product Added.", Toast.LENGTH_SHORT).show();
        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filterProductUser==null){
            filterProductUser = new FilterProductUser(this,filterList);
        }
        return filterProductUser;
    }

    public class ProductUserViewHolder extends RecyclerView.ViewHolder{
        SampleProductUserBinding binding;

        public ProductUserViewHolder(@NonNull View itemView){
            super(itemView);

            binding = SampleProductUserBinding.bind(itemView);
        }
    }
}
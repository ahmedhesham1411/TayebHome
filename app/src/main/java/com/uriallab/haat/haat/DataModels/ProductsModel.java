package com.uriallab.haat.haat.DataModels;

import java.util.List;

public class ProductsModel {

    private ResultEntity result;

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public ResultEntity getResult() {
        return result;
    }

    public class ResultEntity {

        private List<ProductsEntity> products;

        public void setProducts(List<ProductsEntity> products) {
            this.products = products;
        }

        public List<ProductsEntity> getProducts() {
            return products;
        }

        public class ProductsEntity {

            private int StoreID;
            private String product_icon;
            private String product_price;
            private String product_description;
            private String product_name;
            private String place_id;
            private int ProductUID;
            private int product_cat;
            private boolean isSelected;

            public boolean isSelected() {
                return isSelected;
            }

            public void setSelected(boolean selected) {
                isSelected = selected;
            }

            public int getProduct_cat() {
                return product_cat;
            }

            public void setProduct_cat(int product_cat) {
                this.product_cat = product_cat;
            }

            public void setStoreID(int StoreID) {
                this.StoreID = StoreID;
            }

            public void setProduct_icon(String product_icon) {
                this.product_icon = product_icon;
            }

            public void setProduct_price(String product_price) {
                this.product_price = product_price;
            }

            public void setProduct_description(String product_description) {
                this.product_description = product_description;
            }

            public void setProduct_name(String product_name) {
                this.product_name = product_name;
            }

            public void setPlace_id(String place_id) {
                this.place_id = place_id;
            }

            public void setProductUID(int ProductUID) {
                this.ProductUID = ProductUID;
            }

            public int getStoreID() {
                return StoreID;
            }

            public String getProduct_icon() {
                return product_icon;
            }

            public String getProduct_price() {
                return product_price;
            }

            public String getProduct_description() {
                return product_description;
            }

            public String getProduct_name() {
                return product_name;
            }

            public String getPlace_id() {
                return place_id;
            }

            public int getProductUID() {
                return ProductUID;
            }
        }
    }
}
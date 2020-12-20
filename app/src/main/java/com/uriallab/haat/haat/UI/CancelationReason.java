package com.uriallab.haat.haat.UI;

import java.util.List;

public class CancelationReason {
    private ResultBean result;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        private List<ReasonsBean> reasons;

        public List<ReasonsBean> getReasons() {
            return reasons;
        }

        public void setReasons(List<ReasonsBean> reasons) {
            this.reasons = reasons;
        }

        public static class ReasonsBean {

            private int OrderReasonUID;
            private String Reason_Titl;
            private boolean selected;

            public boolean isSelected() {
                return selected;
            }

            public void setSelected(boolean selected) {
                this.selected = selected;
            }

            public int getOrderReasonUID() {
                return OrderReasonUID;
            }

            public void setOrderReasonUID(int OrderReasonUID) {
                this.OrderReasonUID = OrderReasonUID;
            }

            public String getReason_Titl() {
                return Reason_Titl;
            }

            public void setReason_Titl(String Reason_Titl) {
                this.Reason_Titl = Reason_Titl;
            }
        }
    }
}
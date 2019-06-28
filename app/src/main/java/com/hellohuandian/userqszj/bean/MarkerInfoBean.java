package com.hellohuandian.userqszj.bean;

import java.util.List;

public class MarkerInfoBean {

    /**
     * status : 1
     * msg : 请求成功！
     * data : {"id":"1","phone":"400-6060-137","address":"秦皇岛市海港区康乐里6号楼底商","title":"人民广场站","company_name":"线上电柜","open_door":"1","open_title":"营业中","evaluate":5,"usable":10,"cab_typer":"换电站","repair":"-1","jingdu":"119.598099","weidu":"39.938747","name":"人民广场站","battery_num":5,"end_time":"24:00","images":["http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160431_95241_800x400.png","http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160435_69552_800x400.png","http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160438_7489_800x400.png"]}
     */

    private int status;
    private String msg;
    private DataBean data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * phone : 400-6060-137
         * address : 秦皇岛市海港区康乐里6号楼底商
         * title : 人民广场站
         * company_name : 线上电柜
         * open_door : 1
         * open_title : 营业中
         * evaluate : 5
         * usable : 10
         * cab_typer : 换电站
         * repair : -1
         * jingdu : 119.598099
         * weidu : 39.938747
         * name : 人民广场站
         * battery_num : 5
         * end_time : 24:00
         * images : ["http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160431_95241_800x400.png","http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160435_69552_800x400.png","http://img01.halouhuandian.com/hello/hd/shop/2018/2018-11/20181121160438_7489_800x400.png"]
         */

        private String id;
        private String phone;
        private String address;
        private String title;
        private String company_name;
        private String open_door;
        private String open_title;
        private int evaluate;
        private int usable;
        private String cab_typer;
        private String repair;
        private String jingdu;
        private String weidu;
        private String name;
        private int battery_num;
        private String end_time;
        private List<String> images;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany_name() {
            return company_name;
        }

        public void setCompany_name(String company_name) {
            this.company_name = company_name;
        }

        public String getOpen_door() {
            return open_door;
        }

        public void setOpen_door(String open_door) {
            this.open_door = open_door;
        }

        public String getOpen_title() {
            return open_title;
        }

        public void setOpen_title(String open_title) {
            this.open_title = open_title;
        }

        public int getEvaluate() {
            return evaluate;
        }

        public void setEvaluate(int evaluate) {
            this.evaluate = evaluate;
        }

        public int getUsable() {
            return usable;
        }

        public void setUsable(int usable) {
            this.usable = usable;
        }

        public String getCab_typer() {
            return cab_typer;
        }

        public void setCab_typer(String cab_typer) {
            this.cab_typer = cab_typer;
        }

        public String getRepair() {
            return repair;
        }

        public void setRepair(String repair) {
            this.repair = repair;
        }

        public String getJingdu() {
            return jingdu;
        }

        public void setJingdu(String jingdu) {
            this.jingdu = jingdu;
        }

        public String getWeidu() {
            return weidu;
        }

        public void setWeidu(String weidu) {
            this.weidu = weidu;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBattery_num() {
            return battery_num;
        }

        public void setBattery_num(int battery_num) {
            this.battery_num = battery_num;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }
}

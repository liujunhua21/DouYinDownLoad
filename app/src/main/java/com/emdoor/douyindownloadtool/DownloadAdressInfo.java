package com.emdoor.douyindownloadtool;

import java.io.Serializable;

public class DownloadAdressInfo implements Serializable {

        private String httpAdress;

        public String getHttpAdress() {
            return httpAdress;
        }

        public void setHttpAdress(String httpAdress) {
            this.httpAdress = httpAdress;
        }
}

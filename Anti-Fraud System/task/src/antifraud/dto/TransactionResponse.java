package antifraud.dto;

public class TransactionResponse {

    private String result;
    private String info;

    public TransactionResponse() {}

    public TransactionResponse(String result, String info) {
        this.result = result;
        this.info = info;
    }

    public String getResult() { return result; }
    public String getInfo() { return info; }
    public void setResult(String result) { this.result = result; }
    public void setInfo(String info) { this.info = info; }
}

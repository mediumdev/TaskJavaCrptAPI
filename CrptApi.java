import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi
{
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final OkHttpClient client;
    private final Gson gson;
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private long lastRequestTime;

    public CrptApi(TimeUnit timeUnit, int requestLimit)
    {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.lastRequestTime = System.currentTimeMillis();
    }

    static class Document
    {
        private String description;
        private String doc_id;
        private String doc_status;
        private final String doc_type = "LP_INTRODUCE_GOODS";
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;
    }


    static class Product
    {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    public synchronized void createDocument(String description, Document document) throws InterruptedException
    {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRequestTime;

        if (elapsedTime > timeUnit.toMillis(1)) {
            requestCount.set(0);
            lastRequestTime = currentTime;
        }

        if (requestCount.incrementAndGet() > requestLimit) {
            long waitTime = timeUnit.toMillis(1);
            Thread.sleep(waitTime);
            requestCount.set(0);
            lastRequestTime = System.currentTimeMillis();
        }

        document.setDescription(description);

        String jsonBody = gson.toJson(document);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Ok.200");
            } else {
                System.out.println("HTTP response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Демонстрация
    public static void main(String[] args) {
        String description = "description";

        Product product = new Product();
        product.setCertificate_document("string");
        product.setCertificate_document_date("2020-01-23");
        product.setCertificate_document_number("string");
        product.setOwner_inn("string");
        product.setProducer_inn("string");
        product.setProduction_date("2020-01-23");
        product.setTnved_code("string");
        product.setUit_code("string");
        product.setUitu_code("string");

        List<Product> products = Arrays.asList(product);

        Document document = new Document();
        document.setDoc_id("string");
        document.setDoc_status("string");
        document.setImportRequest(true);
        document.setOwner_inn("string");
        document.setParticipant_inn("string");
        document.setProducer_inn("string");
        document.setProduction_date("2020-01-23");
        document.setProduction_type("string");
        document.setProducts(products);
        document.setReg_date("2020-01-23");
        document.setReg_number(reg_number);

        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);

        try
        {
            crptApi.createDocument(description, document);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
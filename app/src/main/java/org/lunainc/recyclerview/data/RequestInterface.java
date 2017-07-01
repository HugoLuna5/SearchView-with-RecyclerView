package org.lunainc.recyclerview.data;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by hugo_ on 02/05/2017.
 */



public interface RequestInterface {

    @GET("api/data.json")
    Call<JSONResponse> getJSON();
}

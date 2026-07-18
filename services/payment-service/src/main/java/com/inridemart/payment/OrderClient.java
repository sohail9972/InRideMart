package com.inridemart.payment;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient; import java.util.UUID;
@Component class OrderClient { final RestClient client; OrderClient(@Value("${inridemart.order.base-url}")String url){client=RestClient.builder().baseUrl(url).build();} void confirm(UUID id,String token){client.post().uri("/api/v1/orders/{id}/payment",id).header("Authorization",token).retrieve().toBodilessEntity();} }

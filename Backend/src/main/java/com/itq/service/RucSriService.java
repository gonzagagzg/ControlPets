package com.itq.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RucSriService {

    private static final String URL_EXISTE_RUC =
            "https://srienlinea.sri.gob.ec/" +
            "sri-catastro-sujeto-servicio-internet/rest/" +
            "ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc=";

    private final HttpClient httpClient;

    public RucSriService() {

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(7))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean existeEnSri(String ruc) {

        if (ruc == null || ruc.isBlank()) {
            return false;
        }

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_EXISTE_RUC + ruc))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header(
                            "User-Agent",
                            "Mozilla/5.0 VeterinariaITQ/1.0"
                    )
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                throw new IllegalStateException(
                        "El SRI no respondió correctamente. Código HTTP: "
                                + response.statusCode()
                );
            }

            String cuerpo =
                    response.body() == null
                            ? ""
                            : response.body().trim();

            if ("true".equalsIgnoreCase(cuerpo)) {
                return true;
            }

            if ("false".equalsIgnoreCase(cuerpo)) {
                return false;
            }

            throw new IllegalStateException(
                    "El SRI devolvió una respuesta inesperada"
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "La consulta al SRI fue interrumpida",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible comunicarse con el SRI",
                    e
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "No fue posible construir la consulta al SRI",
                    e
            );
        }
    }
}
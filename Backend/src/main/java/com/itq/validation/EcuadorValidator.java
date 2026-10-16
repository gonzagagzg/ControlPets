package com.itq.validation;

public final class EcuadorValidator {

    private EcuadorValidator() {
    }

    // =========================================================
    // CÉDULA ECUATORIANA
    // =========================================================

    public static boolean cedulaValida(
            String cedula
    ) {

        if (cedula == null) {
            return false;
        }

        cedula = cedula.trim();

        if (!cedula.matches("\\d{10}")) {
            return false;
        }

        int provincia =
                Integer.parseInt(
                        cedula.substring(0, 2)
                );

        /*
         * Provincias ecuatorianas:
         * 01 - 24
         * 30 se utiliza para ecuatorianos
         * registrados en el exterior.
         */
        if (!(
                (provincia >= 1 && provincia <= 24)
                        || provincia == 30
        )) {
            return false;
        }

        int tercerDigito =
                Character.getNumericValue(
                        cedula.charAt(2)
                );

        /*
         * Para cédula de persona natural
         * el tercer dígito debe ser 0-5.
         */
        if (tercerDigito > 5) {
            return false;
        }

        int suma = 0;

        for (int i = 0; i < 9; i++) {

            int digito =
                    Character.getNumericValue(
                            cedula.charAt(i)
                    );

            if (i % 2 == 0) {

                digito *= 2;

                if (digito > 9) {
                    digito -= 9;
                }
            }

            suma += digito;
        }

        int digitoVerificador =
                (10 - (suma % 10)) % 10;

        int ultimoDigito =
                Character.getNumericValue(
                        cedula.charAt(9)
                );

        return digitoVerificador
                == ultimoDigito;
    }

    // =========================================================
    // RUC ECUATORIANO
    // =========================================================

    public static boolean rucValido(
            String ruc
    ) {

        if (ruc == null) {
            return false;
        }

        ruc = ruc.trim();

        if (!ruc.matches("\\d{13}")) {
            return false;
        }

        int provincia =
                Integer.parseInt(
                        ruc.substring(0, 2)
                );

        if (!(
                (provincia >= 1 && provincia <= 24)
                        || provincia == 30
        )) {
            return false;
        }

        /*
         * Todo RUC debe terminar en
         * establecimiento 001 o superior.
         */
        String establecimiento =
                ruc.substring(10, 13);

        int numeroEstablecimiento =
                Integer.parseInt(
                        establecimiento
                );

        if (numeroEstablecimiento <= 0) {
            return false;
        }

        int tercerDigito =
                Character.getNumericValue(
                        ruc.charAt(2)
                );

        /*
         * PERSONA NATURAL
         * tercer dígito 0 - 5
         *
         * Los primeros 10 dígitos deben
         * formar una cédula válida.
         */
        if (tercerDigito >= 0 &&
                tercerDigito <= 5) {

            String cedula =
                    ruc.substring(0, 10);

            return cedulaValida(cedula);
        }

        /*
         * SOCIEDAD PRIVADA / EXTRANJERA
         * tercer dígito = 9
         */
        if (tercerDigito == 9) {

            return validarRucSociedadPrivada(
                    ruc
            );
        }

        /*
         * ENTIDAD PÚBLICA
         * tercer dígito = 6
         */
        if (tercerDigito == 6) {

            return validarRucPublico(
                    ruc
            );
        }

        return false;
    }

    // =========================================================
    // RUC SOCIEDAD PRIVADA
    // =========================================================

    private static boolean validarRucSociedadPrivada(
            String ruc
    ) {

        int[] coeficientes =
                {4, 3, 2, 7, 6, 5, 4, 3, 2};

        int suma = 0;

        for (int i = 0; i < 9; i++) {

            int digito =
                    Character.getNumericValue(
                            ruc.charAt(i)
                    );

            suma +=
                    digito
                            * coeficientes[i];
        }

        int residuo =
                suma % 11;

        int verificador =
                residuo == 0
                        ? 0
                        : 11 - residuo;

        int digitoReal =
                Character.getNumericValue(
                        ruc.charAt(9)
                );

        return verificador
                == digitoReal;
    }

    // =========================================================
    // RUC SECTOR PÚBLICO
    // =========================================================

    private static boolean validarRucPublico(
            String ruc
    ) {

        int[] coeficientes =
                {3, 2, 7, 6, 5, 4, 3, 2};

        int suma = 0;

        for (int i = 0; i < 8; i++) {

            int digito =
                    Character.getNumericValue(
                            ruc.charAt(i)
                    );

            suma +=
                    digito
                            * coeficientes[i];
        }

        int residuo =
                suma % 11;

        int verificador =
                residuo == 0
                        ? 0
                        : 11 - residuo;

        /*
         * En RUC público el dígito
         * verificador está en posición 9
         * (índice 8).
         */
        int digitoReal =
                Character.getNumericValue(
                        ruc.charAt(8)
                );

        return verificador
                == digitoReal;
    }

    // =========================================================
    // LIMPIEZA
    // =========================================================

    public static String limpiarNumero(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        return valor
                .replaceAll(
                        "[^0-9]",
                        ""
                )
                .trim();
    }
}
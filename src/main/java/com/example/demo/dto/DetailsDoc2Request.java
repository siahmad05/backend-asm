package com.example.demo.dto;

public record DetailsDoc2Request(
        String from,
        String to,
        String login,
        String repres,
        String codeDoc,
        String idEtat,
        boolean all,
        boolean allDocuments,
        String idArticle,
        boolean affichAvanc
) {
}

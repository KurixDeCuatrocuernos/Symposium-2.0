package com.sympos2.services;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.sympos2.models.Obra;
import com.sympos2.repositories.ObraRepository;

@Service
public class ObraService{
	
	@Autowired 
	private MongoTemplate mt;
	
	@Autowired
	private ObraRepository obraRepo;
	
	public List<String> findAllDifferentTemas() {
        List<String> temas = mt.query(Obra.class)
                            .distinct("temas")
                            .as(String.class)
                            .all();
        return temas.stream()
                .map(tema -> Normalizer.normalize(tema, Normalizer.Form.NFD)) // Eliminar acentos
                .map(tema -> tema.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")) // Quitar diacríticos
                .map(String::toLowerCase) // Convertir a minúsculas
                .distinct() // Evitar duplicados después de normalizar
                .collect(Collectors.toList());
    }
}

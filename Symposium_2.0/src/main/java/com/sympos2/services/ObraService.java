package com.sympos2.services;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.sympos2.models.Obra;
import com.sympos2.repositories.ObraRepository;

/**
 * This service implements complex methods for ObraRepository
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 */
@Service
public class ObraService{
	
	@Autowired 
	private MongoTemplate mt;
	
	@Autowired
	private ObraRepository obraRepo;
	
	/**
	 * This method returns a List of all the distinct themes in every Obra object in database, in order to show recommendations when inserts new Obra object.
	 * @return returns a List of themes (String).
	 */
	public List<String> findAllDifferentTemas() {
        List<String> temas = mt.query(Obra.class)
                            .distinct("temas")
                            .as(String.class)
                            .all();
        return temas.stream()
                .map(tema -> Normalizer.normalize(tema, Normalizer.Form.NFD)) 
                .map(tema -> tema.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")) 
                .map(String::toLowerCase) 
                .distinct()
                .collect(Collectors.toList());
    }
}

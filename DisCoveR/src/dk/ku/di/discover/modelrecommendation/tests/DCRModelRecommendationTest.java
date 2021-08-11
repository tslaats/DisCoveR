/**
 * @author Tijs Slaats
 * 
 * DisCoveR: The DCR Graphs process miner.
 * Copyright (C) 2021 Tijs Slaats
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional terms under GNU AGPL version 3 section 7:
 * - By using the program, you provide the copyright holder with permission to mention, discuss, and describe your use of the program in academic publications and accept your responsibility to assist the copyright holder in such publications by providing relevant information.
 *  
 */
package dk.ku.di.discover.modelrecommendation.tests;

import static org.junit.jupiter.api.Assertions.*;
//import org.eclipse.persistence.jaxb.MarshallerProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dk.ku.di.discover.modelrecommendation.DCRModelAdvice;
import dk.ku.di.discover.modelrecommendation.DCRModelRecommendation;

class DCRModelRecommendationTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void testRecommend() {
       JAXBContext jc;
       try {
    	   //jc = JAXBContext.newInstance("DCRAdvice.class", DCRAdvice.class.getClassLoader());
    	   jc = JAXBContext.newInstance("dk.ku.di.discover.modelrecommendation", DCRModelAdvice.class.getClassLoader());
    	   Marshaller m = jc.createMarshaller();
    	   m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
           //m.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
           //m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
       
    	   File f = new File("./logs/morten.xes");
    	   DCRModelRecommendation r = new DCRModelRecommendation();
    	   DCRModelAdvice a = r.recommend(f);
    	   m.marshal(a, System.out);
       } catch (JAXBException e) {
    	   // TODO Auto-generated catch block
    	   e.printStackTrace();
       }
	}
	
	@Test
	void testGetRecommendation() {
		Object input = null;
		try {
			input = new String (Files.readAllBytes( Paths.get("./logs/morten.xes") ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(DCRModelRecommendation.GetRecommendation((String)input));
	}
	
	
	@Test
	void testGetRecommendationSimpleFormat() {
		Object input = new String ("((1,2,3),(1,2,4),(4,3,1,2,3),(1,2))");
		System.out.println("Input: " + input);
		System.out.println("Output: " + DCRModelRecommendation.GetRecommendationSimpleFormat((String)input));
	}		
}


package com.scrapehere.web.application.storepage;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;

import com.google.common.collect.ImmutableMap;
import com.scrapehere.dao.dbobjects.StoreDetails;
import com.scrapehere.dao.lists.ListActionsDao;
import com.scrapehere.dao.similarstore.SimilarStoreActionsDao;
import com.scrapehere.dao.stores.StoreActionsDao;

@Controller
public class StoreApplication {

	private DataSource dataSource;

	@RequestMapping("/stores.html")
	public String processSubmit (@ModelAttribute("storeRequest")StoreRequest storeRequest,
			BindingResult result, SessionStatus status, HttpSession session) {

		final ImmutableMap<String, String> storeInfo = ImmutableMap.<String, String>builder()
				.put("shopatplaces", "http://www.shopatplaces.com/image/data/Shopatplaces_Logo_1.png")
				.put("beyondpinkshop", "http://www.beyondpinkshop.com/image_beyondpink/data/banners/beyondpink-logo.png")
				.build();

		storeRequest.setStoreInfo(storeInfo);

		//update breadcrumblinks
		final List<String> bcStrs = newArrayList();
		bcStrs.add("Home");
		bcStrs.add("Store Account");

		storeRequest.setBreadCrumbStrings(bcStrs);
		final List<String> bcLks = newArrayList();
		bcLks.add("/home.html");
		bcLks.add("");
		storeRequest.setBreadCrumbLinks(bcLks);

		return "storepage/store";
	}

	@RequestMapping(method=RequestMethod.GET, value="/onlinestoresalpha.html")
	public String getStores(@ModelAttribute("storeRequest")StoreRequest storeRequest ,
			BindingResult result, SessionStatus status, HttpSession session) {


		StoreActionsDao storeActionsDao = new StoreActionsDao(dataSource);
		Map<String,List<String>> categoriesByStoreNames  = storeActionsDao.getStoreCategories();
		List<String> allStores =  newArrayList();

		for(String stores :categoriesByStoreNames.keySet()){
			allStores.add(stores);
		}

		final Map<String,List<String>> storebyalphabet = newHashMap();
		String[] alphabet = { "A", "B", "C", "D", "E", "F", "G","H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T","U", "V", "W", "X", "Y", "Z" }; 

		for(int i = 0;i<alphabet.length;i++){

			String letter = alphabet[i];
			List<String> stores=newArrayList();
			for(String store:allStores){
				if(store.startsWith(letter.toLowerCase())){
					stores.add(store);
				}
			}
			storebyalphabet.put(letter,stores);
		}

		//treemap contains sorted stores...key is alphabet and values is stores
		Map<String, List<String>> sortedStores = new TreeMap<String, List<String>>(storebyalphabet);
		storeRequest.setStorebyalphabet(sortedStores);

		session.setAttribute("title", "List of online shopping websites in India on Scrapehere.com");
		session.setAttribute("desc", "Find the list of online shopping sites in India on Scrapehere categorized by alphabetical order");

		return "storecommon/storealphabeticalview";

	}


	/** class to display stores for each category*/
	@RequestMapping(method = RequestMethod.GET, value = "/onlinestores.html")
	public String getStoresofEachCategory(@ModelAttribute("storeRequest")StoreRequest storeRequest,
			BindingResult result, SessionStatus status, HttpSession session) {

		StoreActionsDao storeActionsDao = new StoreActionsDao(dataSource);
		Map<String,List<String>> categoriesByStoreNames  = storeActionsDao.getStoreCategories();
		Map<String, String> storesByCategory =  newHashMap();

		for(String storeName:categoriesByStoreNames.keySet()){

			for(String categories:categoriesByStoreNames.get(storeName)){
				if(storesByCategory.containsKey(categories)){
					String stores = storesByCategory.get(categories);
					stores = stores.concat(","+storeName);
					storesByCategory.put(categories, stores);
				}else{
					storesByCategory.put(categories,storeName);
				}
			}
		}

		Map<String,List<String>> storeNamesByCategory = newHashMap();
		ListActionsDao listActionsDao = new ListActionsDao(dataSource);
		Map<String,String> canonicalCategoryNames = listActionsDao.getCategoryCanonialNames();

		for(String category:storesByCategory.keySet()){
			List<String> stores = new ArrayList<String>(Arrays.asList(storesByCategory.get(category).split(",")));
			category = canonicalCategoryNames.get(category);
			storeNamesByCategory.put(category, stores);
		}

		storeRequest.setStoresbyCategory(storeNamesByCategory);

		return "storecommon/storecategoryview";
	}


	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


}
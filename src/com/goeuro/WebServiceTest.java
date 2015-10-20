package com.goeuro;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Wallace Espindola - wallace.espindola@gmail.com
 *
 */
public class WebServiceTest {

	/**
	 * Default constructor
	 */
	public WebServiceTest() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//String strParam = "Berlin";
		//String strParam = "WrongValueSearch";
		String strParam = args[0];

		WebServiceTest wst = new WebServiceTest();
		wst.SearchGoEuroWebService(strParam);
	}

	private void SearchGoEuroWebService(String strParam) {
		try {
			String urlString = "http://api.goeuro.com/api/v2/position/suggest/en/";
			
			URL url = new URL(urlString + strParam);
			System.out.println("Querying URL: " + url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			if (connection != null) {
				InputStream instream = connection.getInputStream();

				String json = toString(instream);

				instream.close();

				List<JSONObject> jsonList = getJsonValues(json);

				WriteCSVFile(jsonList, strParam);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	private String toString(InputStream is) throws IOException {

		byte[] bytes = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int lidos;
		while ((lidos = is.read(bytes)) > 0) {
			baos.write(bytes, 0, lidos);
		}
		return new String(baos.toByteArray());
	}

	private List<JSONObject> getJsonValues(String jsonString) {

		List<JSONObject> jsonList = new ArrayList<>();
		try {
			JSONArray jsonArr = new JSONArray(jsonString);
			for (Object obj : jsonArr) {
				JSONObject jsonObj = (JSONObject) obj;
				System.out.println("===============================================================================");
				System.out.println(jsonObj);
				jsonList.add(jsonObj);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}

		return jsonList;
	}

	public void WriteCSVFile(List<JSONObject> jsonList, String strParam) {
		try {
			String fileName = "output_file.csv";
			
			File file = new File(fileName);
			if (file.exists()) {
				// Deletes the previous file, if it exists
				file.delete();
			}
			
			FileWriter fileWriter = new FileWriter(file, false);

			if (jsonList != null && !jsonList.isEmpty()) {
				System.out.println("===============================================================================");
				for (Iterator<JSONObject> iterator = jsonList.iterator(); iterator.hasNext();) {
					
					JSONObject jsonObject = (JSONObject) iterator.next();
					
					Integer id = jsonObject.getInt("_id");
					String name = jsonObject.getString("name");
					String type = jsonObject.getString("type");
					JSONObject jsonObjGeoPosition = jsonObject.getJSONObject("geo_position");
					Double latitude = null;
					Double longitude = null;
					if (jsonObjGeoPosition != null) {
						latitude = jsonObjGeoPosition.getDouble("latitude");
						longitude = jsonObjGeoPosition.getDouble("longitude"); 
					}
					String content = (id + "," + name + "," + type + "," + latitude + "," + longitude + "\n");
					
					System.out.println(content);
					
					fileWriter.write(content);
				}
			} else {
				String errorMsg = "No matches are found for query: " + strParam;
				System.out.println(errorMsg);
				fileWriter.write(errorMsg);
			}
			
			fileWriter.close();
			
			System.out.println("===============================================================================");
			System.out.println("Sucessfully finished processing!");
			
			JOptionPane.showMessageDialog(null, "File successfully created", "Finished", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e) {
			System.out.println(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atention", JOptionPane.WARNING_MESSAGE);
		}
	}
}

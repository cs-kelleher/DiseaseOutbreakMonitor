package GroupProject.Team8.DiseaseOutbreakMonitor;

/*
 Taken from: https://github.com/schnapse/android-json-to-requestparams/blob/master/JsonHelper.java
 Convert JSON to RequestParams (Type required by the new submit code)
 */
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonHelper
{
    /**
     * Convert JSONObject or JSONArray to RequestParams
     * @param jsonArray
     * @return
     */
    public static RequestParams toRequestParams(JSONArray jsonArray)
    {
        return mapToRequestParams(arrayToHashMap(jsonArray, ""));
    }

    /**
     * Convert JSONObject to HashMap
     * @param jsonObject
     * @param prefix
     * @return
     */
    private static HashMap<String, String> objectToHashMap(JSONObject jsonObject, String prefix)
    {
        HashMap<String, String> hashMap = new HashMap<>();

        Iterator<String> iterator = jsonObject.keys();

        while (iterator.hasNext())
        {
            String key = iterator.next();

            String tmpPrefix = prefix.isEmpty() ? key : prefix + "[" + key + "]";

            try
            {
                Object value = jsonObject.get(key);

                addParam(hashMap, tmpPrefix, value);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return hashMap;
    }

    /**
     * Convert JSONArray to HashMap
     * @param jsonArray
     * @param prefix
     * @return
     */
    private static HashMap<String, String> arrayToHashMap(JSONArray jsonArray, String prefix)
    {
        HashMap<String, String> hashMap = new HashMap<>();

        for(int i = 0; i < jsonArray.length(); i++)
        {
            String tmpPrefix = prefix + "[" + i + "]";

            try
            {
                Object value = jsonArray.get(i);

                addParam(hashMap, tmpPrefix, value);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

        return hashMap;
    }

    /**
     * Convert HashMap to RequestParams
     * @param map
     * @return
     */
    private static RequestParams mapToRequestParams(HashMap<String, String> map)
    {
        RequestParams params = new RequestParams();

        for(Map.Entry<String, String> entry : map.entrySet())
        {
            params.add(entry.getKey(), entry.getValue());
        }

        return params;
    }

    /**
     * Add param to HashMap
     * @param params
     * @param prefix
     * @param value
     */
    private static void addParam(HashMap<String, String> params, String prefix, Object value)
    {
        if(value instanceof JSONObject)
        {
            JSONObject obj = (JSONObject) value;

            params.putAll(objectToHashMap(obj, prefix));
        }
        else if(value instanceof JSONArray)
        {
            JSONArray array = (JSONArray) value;

            params.putAll(arrayToHashMap(array, prefix));
        }
        else
        {
            params.put(prefix, value.toString());
        }
    }
}
package hci.biominer.util.igv;

import java.net.URL;

public class IGVResource {
	
	//fields
	private String trackName;
	private URL datasetURL;
	//doesn't appear to be anyway to call this url from within IGV?
	private URL infoURL;

	//just for graphs
	private boolean isGraph = false;
	private boolean autoscale = true;
	private String color = null;
	private int height = 0;
	private float minimumDataRange = 0;
	private float maximumDataRange = 0;
	
	//constructors
	/**infoURL can be null*/
	public IGVResource (String trackName, URL datasetURL, URL infoURL, boolean isGraph){
		this.trackName = trackName;
		this.datasetURL = datasetURL;
		this.infoURL = infoURL;
		this.isGraph = isGraph;
	}
	
	//methods
	public void addXMLResource(StringBuilder sb){
		sb.append("<Resource ");
		sb.append("name=\"");
		sb.append(trackName);
		sb.append("\" path=\"");
		sb.append(datasetURL.toString());
		if (infoURL != null){
			sb.append("\" url=\"");
			sb.append(infoURL.toString());
		}
		sb.append("\"  />");
	}
	
	public void addTrackXML(StringBuilder sb) {
		if (isGraph) {
			if (height == 0) height = 40;
			sb.append("\t\t<Track renderer=\"BAR_CHART\" windowFunction=\"none\" ");
			if (autoscale) sb.append("autoScale=\"true\" ");
		}
		else sb.append("\t\t<Track renderer=\"BASIC_FEATURE\" ");
		//id
		sb.append("id=\"");
		sb.append(datasetURL.toString());
		sb.append("\" ");
		//name
		sb.append("name=\"");
		sb.append(trackName);
		sb.append("\" ");
		//color?
		if (color != null) {
			sb.append("color=\"");
			sb.append(color);
			sb.append("\" ");
		}
		//height?
		if (height != 0){
			sb.append("height=\"");
			sb.append(height);
			sb.append("\" ");
		}
		//add DataRange?
		if (minimumDataRange !=0 || maximumDataRange !=0){
			sb.append(" > \n");
			addDataRangeXML(sb);
			sb.append("\t\t</Track>\n");
		}
		else sb.append(" /> \n");
		
		
	}
	
	private void addDataRangeXML(StringBuilder sb) {
		sb.append("\t\t\t<DataRange type=\"LINEAR\" baseline=\"0.0\" drawBaseline=\"true\" ");
		//min
		sb.append("minimum=\"");
		sb.append(minimumDataRange);
		sb.append("\" ");
		//max
		sb.append("maximum=\"");
		sb.append(maximumDataRange);
		sb.append("\" ");
		sb.append(" /> \n");
	}

	//getters and setters
	public String getTrackName() {
		return trackName;
	}
	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}
	public URL getDatasetURL() {
		return datasetURL;
	}
	public void setDatasetURL(URL datasetURL) {
		this.datasetURL = datasetURL;
	}
	public URL getInfoURL() {
		return infoURL;
	}
	public void setInfoURL(URL infoURL) {
		this.infoURL = infoURL;
	}

	public boolean isGraph() {
		return isGraph;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public float getMinimumDataRange() {
		return minimumDataRange;
	}

	public void setMinimumDataRange(float minimumDataRange) {
		this.minimumDataRange = minimumDataRange;
		autoscale = false;
	}

	public float getMaximumDataRange() {
		return maximumDataRange;
	}

	public void setMaximumDataRange(float maximumDataRange) {
		this.maximumDataRange = maximumDataRange;
		autoscale = false;
	}

	public void setGraph(boolean isGraph) {
		this.isGraph = isGraph;
	}
	
	
}

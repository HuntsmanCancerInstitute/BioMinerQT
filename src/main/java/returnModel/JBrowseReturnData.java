package returnModel;

public class JBrowseReturnData {
	String errorMessage;
	String warningMessage;
	Boolean dataToDisplay;
	String pathToRepo;
	
	public JBrowseReturnData(String errorMessage, String warningMessage, Boolean dataToDisplay, String pathToRepo) {
		this.errorMessage = errorMessage;
		this.warningMessage = warningMessage;
		this.dataToDisplay = dataToDisplay;
		this.pathToRepo = pathToRepo;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	public Boolean getDataToDisplay() {
		return dataToDisplay;
	}

	public void setDataToDisplay(Boolean dataToDisplay) {
		this.dataToDisplay = dataToDisplay;
	}

	public String getPathToRepo() {
		return pathToRepo;
	}

	public void setPathToRepo(String pathToRepo) {
		this.pathToRepo = pathToRepo;
	}
	
	
}

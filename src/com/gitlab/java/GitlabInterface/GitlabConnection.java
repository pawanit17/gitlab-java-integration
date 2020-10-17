package com.gitlab.java.GitlabInterface;

import java.util.List;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;

/**
 * @author Pavan Dittakavi
 * A class for encapsulating logic for interacting with Gitlab repositories using the Gitlab4J API.
 */
public class GitlabConnection 
{
	/**
	 * The enumeration that contains India or US exchange indicator.
	 */
	public enum Exchange { EXCHANGE_IN, EXCHANGE_US };

	/**
	 * The reference to the GitLabApi class exposed via Gitlab4J.
	 */
	private GitLabApi gitLabApi = null;

	/**
	 * The name of the branch in the 'gitlab4j-demo-project' that we want to work with.
	 */
	private String branchName = "";

	/**
	 * This is the PROJECT that we are going to do our updates on.
	 */
	private Project gitlabDemoProject = null;

	/**
	 * This is the name of the PROJECT that we are going to do our updates on.
	 */
	private final String GITLAB_DEMO_PROJECT = "gitlab4j-demo-project";

	/**
	 * The empty no argument constructor for this class.
	 */
	public GitlabConnection() 
	{
	}

	/**
	 * @param gitlabHostName The hostname where the Gitlab is configured. If the host is Https based, with
	 * 						 internally supplied certificates, then the CA certificates are to be deployed
	 * 						 in the %JRE_HOME%/lib/security/cacerts' file.
	 * @param userAuthToken	The user token as configured from the Gitlab user account settings page.
	 */
	public void connect( String gitlabHostName, String userAuthToken )
	{
		// Create a GitLabApi instance to communicate with GitLab server
		gitLabApi = new GitLabApi( gitlabHostName, userAuthToken );
	}

	/**
	 *  API for closing the Gitlab connections.
	 */
	public void disconnect() 
	{
		gitLabApi.close();
	}
	
	/**
	 * @param branchName  The branch whose contents would be updated.
	 * @param exchange	  The exchange - IN/US, based on which the file to query shall be identified.
	 * @throws GitLabApiException Any exceptions while working with Gitlab4J API.
	 */
	public void listStocksInIndex( String branchName, Exchange exchange ) throws GitLabApiException
	{
		String stocks = getGitlabFileContent( branchName, exchange );
		
		System.out.println( stocks );		
	}

	/**
	 * @param newStock	The new stock to be added to the index.
	 * @param exchange	The exchange - IN/US, based on which the file to add shall be identified.
	 * @throws GitLabApiException Any exceptions while working with Gitlab4J API.
	 */
	public void addNewStock( String newStock, Exchange exchange ) throws GitLabApiException 
	{
		// Update the existing file with the new content - i.e., new user from Chatbot and commit back.*/
		RepositoryFile updatedStockInfoFile = new RepositoryFile();
		updatedStockInfoFile.setFilePath(getFileNameForExchange( exchange ));
		updatedStockInfoFile.encodeAndSetContent( getUpdatedStockInfo( newStock, exchange) );
		gitLabApi.getRepositoryFileApi().updateFile( gitlabDemoProject.getId(), 
													 updatedStockInfoFile, 
													 branchName, 
													 "New stock: " + newStock + " added to the " + ( exchange == Exchange.EXCHANGE_IN ? "Nifty" : "Nasdaq" ) + " index." );
	}
	
	/**
	 * @param branchName  The branch whose contents would be updated.
	 * @param exchange    The exchange - IN/US, based on which the file to query shall be identified.
	 * @return 			  The persisted stock information as an XML string.
	 * @throws GitLabApiException Any exceptions while working with Gitlab4J API.
	 */
	private String getGitlabFileContent( String branchName, Exchange exchange ) throws GitLabApiException 
	{
		this.branchName = branchName;

		// We need to update the 'gitlab4j-demo-project' project. So we get the corresponding Project object from Gitlab interface.
		List<Project> projects = gitLabApi.getProjectApi().getProjects( GITLAB_DEMO_PROJECT );
		
		if( projects.size() == 0 )
		{
			throw new GitLabApiException("No project with the name " + GITLAB_DEMO_PROJECT + " found.");
		}

		// This will be the 'gitlab4j-demo-project' API.
		gitlabDemoProject = projects.get(0);

		// Get the current stock market company list from the master branch.
		RepositoryFile stockIndices = gitLabApi.getRepositoryFileApi().getFile( gitlabDemoProject.getId(), 
																				getFileNameForExchange( exchange ), 
																				this.branchName);

		return stockIndices.getDecodedContentAsString();
	}	

	/**
	 * @param newStock The new stock to be added to the index.
	 * @param exchange The exchange - IN/US, based on which the file to query shall be identified.
	 * @return The updated stock information as an XML string.
	 * @throws GitLabApiException Any exceptions while working with Gitlab4J API.
	 */
	private String getUpdatedStockInfo(String newStock, Exchange exchange) throws GitLabApiException
	{
		String stockInfo = getGitlabFileContent( this.branchName, exchange );
		
		int n = stockInfo.indexOf("</stocks>");
		
		String newStockXMLElement = "<company>" + newStock + "</company>";

		return stockInfo.substring(0, n-1) + "\n" + "    " + newStockXMLElement + "\n" + stockInfo.substring(n);
	}
	
	/**
	 * @param exchange The ENUM value for which the corresponding XML file will be identified.
	 * @return Either the string path to nifty50.xml or nasdaq100.xml depending on the exchange passed.
	 */
	private String getFileNameForExchange( Exchange exchange ) 
	{	
		if( exchange == Exchange.EXCHANGE_IN )
		{
			return "stocks/nifty50.xml";
		}
		
		return "stocks/nasdaq100.xml";
	}
}

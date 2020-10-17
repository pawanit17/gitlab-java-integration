package com.gitlab.java.driver;

import com.gitlab.java.GitlabInterface.GitlabConnection;
import com.gitlab.java.GitlabInterface.GitlabConnection.Exchange;

public class GitlabIntegrationDriver 
{
	/**
	 * The driver method for interacting with APIs exposed by GitlabConnection.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		GitlabConnection connection = new GitlabConnection();
		
		try
		{
			connection.connect("https://gitlab.com/", "tyxU8HcMN_trm9oX3s8j");
			
			// Gitlab File Reads
			connection.listStocksInIndex( "master", Exchange.EXCHANGE_IN );
			connection.listStocksInIndex( "master", Exchange.EXCHANGE_US );
			
			// Gitlab File Updates
			connection.addNewStock("FLIPKART", Exchange.EXCHANGE_IN );
			connection.addNewStock("Edison Corporation", Exchange.EXCHANGE_US );

			// Gitlab File Reads again
			connection.listStocksInIndex( "master", Exchange.EXCHANGE_IN );
			connection.listStocksInIndex( "master", Exchange.EXCHANGE_US );
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			connection.disconnect();
		}	
	}
}



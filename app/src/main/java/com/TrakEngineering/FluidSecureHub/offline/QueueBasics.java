package com.TrakEngineering.FluidSecureHub.offline;//----------------------------------------------------------------------------------
// Microsoft Developer & Platform Evangelism
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
// THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
// EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
//----------------------------------------------------------------------------------
// The example companies, organizations, products, domain names,
// e-mail addresses, logos, people, places, and events depicted
// herein are fictitious.  No association with any real company,
// organization, product, domain name, email address, logo, person,
// places, or events is intended or should be inferred.
//----------------------------------------------------------------------------------

import android.content.Context;
import android.content.SharedPreferences;

import com.TrakEngineering.FluidSecureHub.AppConstants;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

/**
 * This sample illustrates basic usage of the Azure queue storage service.
 */
public class QueueBasics {

    private static final String TAG = "QueueBasics ";

     public static void addMessageOnQueue(Context ctx,String jsonString, String queType)
     {
         try
         {
             String queTitle;

             SharedPreferences sharedPref = ctx.getSharedPreferences(AppConstants.sharedPref_AzureQueueDetails, Context.MODE_PRIVATE);
             String QueueName = sharedPref.getString("QueueName", "");
             String QueueNameForTLD = sharedPref.getString("QueueNameForTLD", "");
             String QueueConnectionStringValue = sharedPref.getString("QueueConnectionStringValue", "");

             if(queType.equalsIgnoreCase("TLD"))
                 queTitle=QueueNameForTLD;
             else
                 queTitle=QueueName;

             final String storageConnectionString =QueueConnectionStringValue;

             /*final String storageConnectionString =
                     "DefaultEndpointsProtocol=https;" +
                             "AccountName=trakqueuestorage;" +
                             "AccountKey=mWSqEL039ixaaITLg2nGiSiE9nXMOuDBt8qtDfVRDgNA1Zl6fCyn90zCJCrvvbJkn/PptlCxhbD4liGv/y5GXg==";
               */

             // Retrieve storage account from connection-string.
             CloudStorageAccount storageAccount =
                     CloudStorageAccount.parse(storageConnectionString);

             // Create the queue client.
             CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

             // Retrieve a reference to a queue.
             CloudQueue queue = queueClient.getQueueReference(queTitle);//"testqueue"

             // Create the queue if it doesn't already exist.
             //queue.createIfNotExists();

             //String jsonStr="{\"TransactionsModelsObj\":[{\"AppInfo\":\" Version:0.53.8.60 Samsung SM-T385 Android 8.1.0 \",\"CurrentHours\":\"\",\"CurrentOdometer\":\"\",\"FuelQuantity\":\"3.2\",\"HubId\":\"4438\",\"Id\":\"3\",\"OnlineTransactionId\":\"\",\"PersonId\":\"1329\",\"PersonPin\":\"123\",\"Pulses\":\"32\",\"SiteId\":\"157\",\"TransactionDateTime\":\"2019-08-08 14:22\",\"TransactionFrom\":\"AP\",\"VehicleId\":\"131\"},{\"AppInfo\":\" Version:0.53.8.60 Samsung SM-T385 Android 8.1.0 \",\"CurrentHours\":\"25\",\"CurrentOdometer\":\"252\",\"FuelQuantity\":\"3.2\",\"HubId\":\"4438\",\"Id\":\"4\",\"OnlineTransactionId\":\"\",\"PersonId\":\"1329\",\"PersonPin\":\"123\",\"Pulses\":\"32\",\"SiteId\":\"157\",\"TransactionDateTime\":\"2019-08-08 14:23\",\"TransactionFrom\":\"AP\",\"VehicleId\":\"140\"},{\"AppInfo\":\" Version:0.53.8.60 Samsung SM-T385 Android 8.1.0 \",\"CurrentHours\":\"\",\"CurrentOdometer\":\"\",\"FuelQuantity\":\"3.2\",\"HubId\":\"4438\",\"Id\":\"5\",\"OnlineTransactionId\":\"\",\"PersonId\":\"1329\",\"PersonPin\":\"123\",\"Pulses\":\"32\",\"SiteId\":\"157\",\"TransactionDateTime\":\"2019-08-08 14:24\",\"TransactionFrom\":\"AP\",\"VehicleId\":\"131\"},{\"AppInfo\":\" Version:0.53.8.60 Samsung SM-T385 Android 8.1.0 \",\"CurrentHours\":\"\",\"CurrentOdometer\":\"\",\"FuelQuantity\":\"2.4\",\"HubId\":\"4438\",\"Id\":\"6\",\"OnlineTransactionId\":\"\",\"PersonId\":\"1329\",\"PersonPin\":\"123\",\"Pulses\":\"24\",\"SiteId\":\"157\",\"TransactionDateTime\":\"2019-08-08 14:26\",\"TransactionFrom\":\"AP\",\"VehicleId\":\"131\"}]}";

             // Create a message and add it to the queue.
             CloudQueueMessage message = new CloudQueueMessage(jsonString);
             queue.addMessage(message);
         }
         catch (Exception e)
         {
             e.printStackTrace();
             if (AppConstants.GenerateLogs)
                 AppConstants.WriteinFile(TAG + "addMessageOnQueue Exception: " + e.getMessage());
         }
     }

}

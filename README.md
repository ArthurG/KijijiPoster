#Kijiji Poster

Kijiji Poster is a desktop application written in [Selenium](http://www.seleniumhq.org/) (Java) that posts (and reposts) Kijiji ads using multiple accounts. Every time an ad is posted, the old version is deleted off Kijiji. Reposting an ad moves the ad to the top of search results. 

This program use an .xlsx spreadsheet file to import product details [(using Apache POI library)](https://poi.apache.org). 

#Usage

1. Ensure .classpath points correctly to the POI and Selenium libraries
2. Modify LOCATION constant inside KijijiList.java to your address/city/postal code
3. Create individual folders for product images 
4. Change kijiji.xlsx spreadsheet with correct account infomation and item details. Don't forget to include the product image folders created in step 3!

###Sample Kijiji.xlsx
![Kijiji.xlsx preview](https://raw.githubusercontent.com/guoarthur/KijijiPoster/master/TemplatePreview.JPG "This is a sample kijiji.xlsx input file")
A sample directory is provided inside the Kijiji folder

#Bugs
- Selenium does not have support for flash, therefore cannot upload images. This program currently uses java.awt.Robot to copy and paste files into the upload prompt (workaround)
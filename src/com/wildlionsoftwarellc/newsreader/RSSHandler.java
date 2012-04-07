package com.wildlionsoftwarellc.newsreader;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class RSSHandler extends DefaultHandler {
	
	public class NewsItem {
        public String title;
        public String link;
        public String description;
        public String pubDate;
        public String thumbUrl;
        
        @Override
        public String toString() {
            return title;
        }
    }
	private StringBuffer buf;
    private ArrayList<NewsItem> feedItems;
    private NewsItem item;
    
    private boolean inItem = false;
    
    public ArrayList<NewsItem> getParsedItems() {
        return feedItems;
    }
    
    //Called at the head of each new element
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        if("channel".equals(name)) {
            feedItems = new ArrayList<NewsItem>();
        } else if("item".equals(name)) {
            item = new NewsItem();
            inItem = true;
        } else if("title".equals(name) && inItem) {
            buf = new StringBuffer();
        } else if("link".equals(name) && inItem) {
            buf = new StringBuffer();
        } else if("description".equals(name) && inItem) {
            buf = new StringBuffer();
        } else if("pubDate".equals(name) && inItem) {
        	buf = new StringBuffer();
        }
    }
    
    //Called at the tail of each element end
    @Override
    public void endElement(String uri, String name, String qName) {
        if("item".equals(name)) {
            feedItems.add(item);
            inItem = false;
        } else if("title".equals(name) && inItem) {
            item.title = buf.toString();
        } else if("link".equals(name) && inItem) {
            item.link = buf.toString();
        } else if("description".equals(name) && inItem) {
            item.description = buf.toString();
            Document doc = Jsoup.parse(buf.toString());
            Element img = doc.select("img").first();
            String testUrl = img.attr("src");
            if(testUrl.length() > 0){
            	if(!testUrl.startsWith("http:")){
            		testUrl = "http:" + testUrl;
            	}
            	item.thumbUrl = testUrl;
            }
        } else if("pubDate".equals(name) && inItem) {
        	item.pubDate = buf.toString();
        }
        
        buf = null;
    }
    
    //Called with character data inside elements
    @Override
    public void characters(char ch[], int start, int length) {
        //Don't bother if buffer isn't initialized
        if(buf != null) {
            for (int i=start; i<start+length; i++) {
                buf.append(ch[i]);
            }
        }
    }
}

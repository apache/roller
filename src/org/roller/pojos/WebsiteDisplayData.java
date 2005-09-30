/*
 * Created on Apr 15, 2003
 */
package org.roller.pojos;


/**
 * For most popular website display on Roller main page.
 * The id property is the website's name.
 *
 * @author David M Johnson
 *
 * @ejb:bean name="WebsiteDisplayData"
 * @struts.form include-all="true"
 */
public class WebsiteDisplayData extends PersistentObject {
    static final long serialVersionUID = 5264701383470813687L;
    
    private String mId;
    private String mWebsiteName = null;
    private String mWebsiteHandle = null;
    private Integer mHits = new Integer(0);
    
    /**
     *
     */
    public WebsiteDisplayData() {
        super();
    }
    
    /**
     *
     */
    public WebsiteDisplayData(
            String id,
            String websiteName,
            String websiteHandle,
            Integer hits) {
        super();
        mId = id;
        mWebsiteName = websiteName;
        mWebsiteHandle = websiteHandle;
        mHits = hits;
    }
    
    /**
     * No-op.
     * @see org.roller.pojos.PersistentObject#setData(org.roller.pojos.PersistentObject)
     */
    public void setData(PersistentObject vo) {
    }
    
    /**
     * @ejb:persistent-field
     */
    public String getId() {
        return mId;
    }
    
    /**
     * @see org.roller.pojos.PersistentObject#setId(java.lang.String)
     */
    public void setId(String id) {
        mId = id;
    }
        
    /**
     * @ejb:persistent-field
     */
    public Integer getHits() {
        return mHits;
    }
    
    /**
     * @param integer
     */
    public void setHits(Integer integer) {
        mHits = integer;
    }
    
    
    /**
     * @return Returns the title.
     */
    public String getWebsiteName() {
        return mWebsiteName;
    }
    
    /**
     * @param title The title to set.
     */
    public void setWebsiteName(String name) {
        mWebsiteName = name;
    }
    
    public String getWebsiteHandle() {
        return mWebsiteHandle;
    }
    
    public void setWebsiteHandle(String mWebsiteHandle) {
        this.mWebsiteHandle = mWebsiteHandle;
    }
}

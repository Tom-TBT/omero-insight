/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.SearchModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.SearchThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/**
 * A DataBrowserModel for search results
 * 
 * @author  Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class AdvancedResultSearchModel extends DataBrowserModel {

    /** Holds all the ImageDisplays */
    List<ImageDisplay> displays = new ArrayList<ImageDisplay>();

    /** Holds the thumbnails */
    Map<DataObject, Thumbnail> thumbs = new HashMap<DataObject, Thumbnail>();

    /** References to the tables to be notified when thumbs have been loaded */
    List<SearchResultTable> tables = new ArrayList<SearchResultTable>();

    /**
     * Creates a new instance.
     * 
     * @param results
     *            The results to display.
     */
    public AdvancedResultSearchModel(AdvancedSearchResultCollection results) {

        super(null);
        if (results == null)
            throw new IllegalArgumentException("No results.");

        displays.addAll(createDisplays(results.getDataObjects(-1,
                ProjectData.class)));
        displays.addAll(createDisplays(results.getDataObjects(-1,
                DatasetData.class)));

        List<DataObject> imgs = results.getDataObjects(-1, ImageData.class);
        List<ImageDisplay> imgNodes = createDisplays(imgs);
        displays.addAll(imgNodes);

        displays.addAll(createDisplays(results.getDataObjects(-1,
                ScreenData.class)));
        displays.addAll(createDisplays(results.getDataObjects(-1,
                PlateData.class)));

        browser = BrowserFactory.createBrowser(displays);
    }

    /**
     * Registers a table to be notified when thumbs have been loaded
     * @param table
     */
    public void registerTable(SearchResultTable table) {
        this.tables.add(table);
    }

    /**
     * Creates the {@link ImageDisplay}s for the given {@link DataObject}s
     * @param dataObjs
     * @return
     */
    private List<ImageDisplay> createDisplays(Collection<DataObject> dataObjs) {
        List<ImageDisplay> result = new ArrayList<ImageDisplay>();

        for (DataObject dataObj : dataObjs) {
            ImageDisplay d = null;

            if (dataObj instanceof ImageData) {
                d = new ImageNode("", dataObj, null);
            } else if (dataObj instanceof ProjectData
                    || dataObj instanceof DatasetData
                    || dataObj instanceof ScreenData
                    || dataObj instanceof PlateData) {
                d = new ImageSet("", dataObj);
            }

            if (d != null)
                result.add(d);
        }

        return result;
    }

    @Override
    void loadData(boolean refresh, Collection ids) {
        loadThumbs();
    }

    /**
     * Starts a loader for each group to load the thumbnails
     */
    private void loadThumbs() {

        Map<Long, List<DataObject>> map = new HashMap<Long, List<DataObject>>();
        for (ImageDisplay d : displays) {
            DataObject obj = (DataObject) d.getHierarchyObject();
            List<DataObject> objs = map.get(obj.getGroupId());
            if (objs == null) {
                objs = new ArrayList<DataObject>();
                map.put(obj.getGroupId(), objs);
            }
            objs.add(obj);
        }

        for (Entry<Long, List<DataObject>> e : map.entrySet()) {
            List<DataObject> imgs = new ArrayList<DataObject>();
            for (DataObject dataObj : e.getValue()) {
                if (dataObj instanceof ImageData)
                    imgs.add((ImageData) dataObj);
            }

            if (!imgs.isEmpty()) {
                SearchThumbnailLoader loader = new SearchThumbnailLoader(
                        component, new SecurityContext(e.getKey()), imgs, this);
                loader.load();
            }
        }
    }

    /**
     * Creates a concrete loader.
     * 
     * @see DataBrowserModel#createDataLoader(boolean, Collection)
     */
    protected List<DataBrowserLoader> createDataLoader(boolean refresh,
            Collection ids) {
        return null;
    }

    /**
     * Returns the type of this model.
     * 
     * @see DataBrowserModel#getType()
     */
    protected int getType() {
        return DataBrowserModel.SEARCH;
    }

    /**
     * @see DataBrowserModel#getNodes()
     */
    protected List<ImageDisplay> getNodes() {
        return displays;
    }

    /**
     * Add a thumbnail for a certain image
     * @param imgId
     * @param img
     */
    public void setThumbnail(long imgId, BufferedImage img) {
        System.out.println(imgId);
        System.out.println(img.getHeight());

        for (ImageDisplay d : displays) {
            System.out.println(d);
            if (d.getHierarchyObject() instanceof ImageData
                    && ((ImageData) d.getHierarchyObject()).getId() == imgId) {
                ImageData refObj = (ImageData) d.getHierarchyObject();
                ThumbnailProvider thumb = new ThumbnailProvider(refObj);
                thumb.setFullScaleThumb(img);
                thumbs.put(refObj, thumb);
                break;
            }
        }
    }

    /**
     * Get the thumbnail for a certain image
     * @param refObj
     * @return
     */
    public Thumbnail getThumbnail(DataObject refObj) {
        return thumbs.get(refObj);
    }

    /**
     * Notifies the tables that the thumbnails have been loaded
     */
    public void notifyThumbsLoaded() {
        for (SearchResultTable table : tables) {
            table.refreshTable();
        }
    }

}

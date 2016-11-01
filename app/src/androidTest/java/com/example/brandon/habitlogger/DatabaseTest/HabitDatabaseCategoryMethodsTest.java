package com.example.brandon.habitlogger.DatabaseTest;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class HabitDatabaseCategoryMethodsTest extends AndroidTestCase {
    private HabitDatabase db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new HabitDatabase(context);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetNumberOfCategories(){
        db.addCategory(new HabitCategory("color", "one"));
        db.addCategory(new HabitCategory("color", "two"));
        db.addCategory(new HabitCategory("color", "three"));
        assertEquals(3, db.getNumberOfCategories());
    }

    public void testAddCategory(){
        HabitCategory expectedCategory = new HabitCategory("color", "name");
        long id = db.addCategory(expectedCategory);

        HabitCategory actualCategory = db.getCategory(id);

        assertNotNull(actualCategory);
        assertEquals(expectedCategory, actualCategory);
        assertEquals(expectedCategory.getDatabaseId(), actualCategory.getDatabaseId());
    }

    public void testGetCategoryIdFromIndex(){
        db.addCategory(new HabitCategory("color", "one"));   // Index 0
        db.addCategory(new HabitCategory("color", "two"));   // Index 1
        long expectedId = db.addCategory(new HabitCategory("color", "three")); // Index 2

        long actualId = db.getCategoryIdFromIndex(2);

        assertEquals(expectedId, actualId);
    }

    public void testGetCategoryIdByObject(){
        HabitCategory category = new HabitCategory("color", "three");
        long expectedId = db.addCategory(category);

        long actualId = db.getCategoryIdByObject(category);
        assertEquals(expectedId, actualId);
    }

    public void testSearchCategoryIdsByName(){
        db.addCategory(new HabitCategory("color", "one"));
        long expectedId = db.addCategory(new HabitCategory("color", "two"));
        db.addCategory(new HabitCategory("color", "three"));

        long[] ids = db.searchCategoryIdsByName("wo");

        assertEquals(expectedId, ids[0]);
    }

    public void testGetCategory(){
        db.addCategory(new HabitCategory("color", "one"));   // Index 0
        db.addCategory(new HabitCategory("color", "two"));   // Index 1

        HabitCategory expectedCategory = new HabitCategory("color", "three");
        long targetId = db.addCategory(expectedCategory); // Index 2

        HabitCategory actualCategory = db.getCategory(targetId);

        assertNotNull(actualCategory);
        assertEquals(expectedCategory, actualCategory);
        assertEquals(expectedCategory.getDatabaseId(), actualCategory.getDatabaseId());
    }

    public void testUpdateCategoryName(){
        HabitCategory origCategory = new HabitCategory("color", "oldName");

        long categoryId = db.addCategory(origCategory);
        long rowCount   = db.updateCategoryName(categoryId, "newName");

        HabitCategory actualCategory = db.getCategory(categoryId);

        assertEquals(new HabitCategory("color", "newName"), actualCategory);
        assertEquals(1, rowCount);
    }

    public void testUpdateCategoryColor(){
        HabitCategory origCategory = new HabitCategory("oldColor", "name");

        long categoryId = db.addCategory(origCategory);
        long rowCount = db.updateCategoryColor(categoryId, "newColor");

        HabitCategory actualCategory = db.getCategory(categoryId);

        assertEquals(new HabitCategory("newColor", "name"), actualCategory);
        assertEquals(1, rowCount);
    }

    public void testUpdateCategory(){
        // Add the "old" category
        HabitCategory origCategory = new HabitCategory("oldColor", "oldName");
        long categoryId = db.addCategory(origCategory);

        // Update the "old" category with the "new" one
        HabitCategory newCategory  = new HabitCategory("newColor", "newName");
        long rowCount = db.updateCategory(categoryId, newCategory);

        // Fetch the actual category at the categoryId
        HabitCategory actualCategory = db.getCategory(categoryId);

        // Ensure the updated category is as expectd
        assertEquals(new HabitCategory("newColor", "newName"), actualCategory);
        assertEquals(1, rowCount);
    }

    public void testDeleteCategory(){
        HabitCategory origCategory = new HabitCategory("color", "name");
        long categoryId = db.addCategory(origCategory);

        long rowCount = db.deleteCategory(categoryId);

        assertEquals(null, db.getCategory(categoryId));
        assertEquals(1, rowCount);
    }
}
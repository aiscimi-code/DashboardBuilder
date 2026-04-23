package com.dashboard.builder.data.model

/**
 * Utility object for managing tab naming and creation.
 * Tab naming scheme: A-Z (26), then A1-Z1, A2-Z2, ... up to 256 tabs.
 */
object TabManager {
    const val MAX_TABS = 256
    const val TABS_PER_PAGE = 8
    private const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    
    /**
     * Generate all possible tab IDs up to MAX_TABS
     */
    fun generateAllTabIds(): List<String> {
        val tabIds = mutableListOf<String>()
        
        // First: A-Z (26 tabs)
        for (i in 0 until 26) {
            tabIds.add(LETTERS[i].toString())
        }
        
        // Then: A1-Z1, A2-Z2, ... (230 more tabs = 256 - 26)
        var suffix = 1
        while (tabIds.size < MAX_TABS) {
            for (i in 0 until 26) {
                if (tabIds.size >= MAX_TABS) break
                tabIds.add("${LETTERS[i]}$suffix")
            }
            suffix++
        }
        
        return tabIds
    }
    
    /**
     * Get the next available tab ID given a list of existing tab IDs
     */
    fun getNextTabId(existingIds: List<String>): String? {
        if (existingIds.size >= MAX_TABS) return null
        
        val allIds = generateAllTabIds()
        return allIds.find { it !in existingIds }
    }
    
    /**
     * Calculate which page of tabs to show (0-indexed)
     * @param selectedTabId The currently selected tab
     * @param totalTabs Total number of tabs
     * @return The page index to display
     */
    fun getPageForTab(selectedTabId: String, totalTabs: Int): Int {
        val allIds = generateAllTabIds()
        val tabIndex = allIds.indexOf(selectedTabId)
        if (tabIndex < 0) return 0
        return tabIndex / TABS_PER_PAGE
    }
    
    /**
     * Get the tab IDs to display for a given page
     * @param currentPage The page number (0-indexed)
     * @param totalTabs Total number of tabs
     * @param hasAddButton Whether to include the "+" add button
     */
    fun getTabsForPage(currentPage: Int, totalTabs: Int, hasAddButton: Boolean): List<String> {
        val allIds = generateAllTabIds()
        val startIndex = currentPage * TABS_PER_PAGE
        val endIndex = minOf(startIndex + TABS_PER_PAGE, totalTabs)
        
        if (startIndex >= totalTabs) {
            return if (hasAddButton && totalTabs < MAX_TABS) listOf("+") else emptyList()
        }
        
        return allIds.subList(startIndex, endIndex)
    }
    
    /**
     * Check if the "+" add button should be shown
     */
    fun canAddMoreTabs(totalTabs: Int): Boolean {
        return totalTabs < MAX_TABS
    }
    
    /**
     * Get total number of pages needed
     */
    fun getTotalPages(totalTabs: Int, hasAddButton: Boolean): Int {
        val effectiveTabs = if (hasAddButton && totalTabs < MAX_TABS) totalTabs + 1 else totalTabs
        return ((effectiveTabs - 1) / TABS_PER_PAGE) + 1
    }
}
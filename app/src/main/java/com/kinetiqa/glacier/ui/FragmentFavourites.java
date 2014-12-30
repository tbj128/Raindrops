package com.kinetiqa.glacier.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetiqa.glacier.R;
import com.kinetiqa.glacier.components.Bookmark;
import com.kinetiqa.glacier.components.MenuErrorMessage;
import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.core.StatisticsManager;
import com.kinetiqa.glacier.core.connection.ConnectionManager;
import com.kinetiqa.glacier.database.DatabaseHelper;
import com.kinetiqa.glacier.dialogs.DialogInfo;
import com.kinetiqa.glacier.menu.Menu;
import com.kinetiqa.glacier.menu.MenuFolder;
import com.kinetiqa.glacier.menu.MenuItem;
import com.kinetiqa.glacier.menu.MenuManager;
import com.kinetiqa.glacier.utils.TimeConversion;
import com.kinetiqa.glacier.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class FragmentFavourites extends Fragment {

    private View fragmentView;
    List<MenuItem> menuItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment, container, false);
        this.fragmentView = view;
        init();
        return view;
    }

    private void init() {
        menuItems = new ArrayList<MenuItem>();

        List<Bookmark> bookmarks = DatabaseHelper.getInstance(getActivity()).getAllBookmarks();
        for (Bookmark bookmark : bookmarks) {
            Menu m = MenuManager.getInstance().findMenu(bookmark.getItemId());
            if (m != null) {
                if (m instanceof MenuItem)
                    menuItems.add((MenuItem) m);
            }
        }

        Button backButton = (Button) getActivity().findViewById(R.id.nav_back_btn);
        ImageButton homeButton = (ImageButton) getActivity().findViewById(R.id.nav_home_btn);
        backButton.setVisibility(View.GONE);
        homeButton.setVisibility(View.VISIBLE);

        initMainMenuBreadcrumb();
        initMainMenuStats();

        RelativeLayout mainMetaAreaLayout = (RelativeLayout) fragmentView.findViewById(R.id.main_meta_area);
        mainMetaAreaLayout.setVisibility(View.GONE);

        TextView noItemsPrompt = (TextView) fragmentView.findViewById(R.id.main_empty);
        ListView mainMenuListView = (ListView) fragmentView.findViewById(R.id.main_list_menu);

        if (menuItems.size() == 0) {
            noItemsPrompt.setVisibility(View.VISIBLE);
            mainMenuListView.setVisibility(View.GONE);
        } else {
            noItemsPrompt.setVisibility(View.GONE);
            mainMenuListView.setVisibility(View.VISIBLE);

            mainMenuListView.setAdapter(mainMenuAdapter);
            mainMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Menu openedMenu = menuItems.get(position);
                    MenuErrorMessage errorMessage = getOpenMenuErrorMessages(openedMenu);
                    if (errorMessage != null) {
                        DialogInfo infoDialog = new DialogInfo(getActivity(), errorMessage.getTitle(), errorMessage.getDesc());
                        infoDialog.show();
                        return;
                    }

                    if (openedMenu instanceof MenuFolder) {
                        MenuManager.getInstance().openedMenu(openedMenu);
                        init();
                    } else {
                        Intent i = null;
                        if (((MenuItem) openedMenu).getMediaType() == MenuManager.MEDIA_VIDEO) {
                            i = new Intent(getActivity(), MediaVideo.class);
                            i.putExtra("id", openedMenu.getID());
                        } else if (((MenuItem) openedMenu).getMediaType() == MenuManager.MEDIA_DOCUMENT) {
                            i = new Intent(getActivity(), MediaDocument.class);
                            i.putExtra("id", openedMenu.getID());
                        }

                        if (i != null) {
                            startActivity(i);
                        }
                    }
                }
            });
        }
    }

    private MenuErrorMessage getOpenMenuErrorMessages(Menu menu) {
        boolean isPermitted = DatabaseHelper.getInstance(getActivity()).isPermitted(menu.getID());
        if (!isPermitted) {
            return new MenuErrorMessage("Locked", "This item has been locked by the trainer.");
        }

        MenuErrorMessage notCompletedPreRequisites = MenuManager.getInstance().getNotCompletedPreRequisites(getActivity(), menu);
        if (notCompletedPreRequisites != null) {
            return notCompletedPreRequisites;
        }

        return null;
    }

    private void initMainMenuBreadcrumb() {
        LinearLayout breadcrumbContainer = (LinearLayout) fragmentView.findViewById(R.id.breadcrumb_container);
        breadcrumbContainer.removeAllViews();
    }

    private void initMainMenuStats() {
        RelativeLayout mainMenuStatsContainer = (RelativeLayout) fragmentView.findViewById(R.id.main_meta_area_right);
        mainMenuStatsContainer.setVisibility(View.GONE);
    }


    private BaseAdapter mainMenuAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return menuItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_main_menu, parent, false);

            MenuItem menu = menuItems.get(position);

            TextView title = (TextView) view.findViewById(R.id.item_menu_title);
            title.setText(menu.getTitle());

            TextView desc = (TextView) view.findViewById(R.id.item_menu_desc);
            if (menu.getMediaType() == MenuManager.MEDIA_VIDEO) {
                long durationOfVideo = Utils.getLengthOfVideoFileMilliseconds(getActivity(), Config.MENU_MEDIA_PATH_PREFIX + ((MenuItem) menu).getMediaName());
                desc.setText("(" + TimeConversion.convertMillisecondsToTime(durationOfVideo) + ") " + menu.getDesc());
            } else {
                desc.setText(menu.getDesc());
            }

            Integer numSecondsViewingMenu = StatisticsManager.getInstance(getActivity()).getNumSecondsViewingMenu(menu);
            TextView achievement_minutes = (TextView) view
                    .findViewById(R.id.item_menu_stats_time);
            achievement_minutes.setText(TimeConversion
                    .secondsToTime(numSecondsViewingMenu));

            RelativeLayout statsCompletedContainer = (RelativeLayout) view.findViewById(R.id.item_menu_stats_completed_container);
            statsCompletedContainer.setVisibility(View.GONE);

            if (menu instanceof MenuItem) {
                ImageView typeImageView = (ImageView) view.findViewById(R.id.item_menu_type);
                // ImageView completedImageView = (ImageView) view.findViewById(R.id.item_menu_completed);
                ImageView favouritedImageView = (ImageView) view.findViewById(R.id.item_menu_favourited);

                boolean isCompleted = DatabaseHelper.getInstance(getActivity()).isCompleted(menu.getID());
                if (isCompleted) {
                    // completedImageView.setVisibility(View.VISIBLE);
                    if (((MenuItem) menu).getMediaType() == MenuManager.MEDIA_VIDEO) {
                        typeImageView.setImageResource(R.drawable.ic_action_video_green_complete);
                    } else if (((MenuItem) menu).getMediaType() == MenuManager.MEDIA_DOCUMENT) {
                        typeImageView.setImageResource(R.drawable.ic_action_doc_green_complete);
                    }

                } else {
                    if (((MenuItem) menu).getMediaType() == MenuManager.MEDIA_VIDEO) {
                        typeImageView.setImageResource(R.drawable.ic_action_video_green);
                    } else if (((MenuItem) menu).getMediaType() == MenuManager.MEDIA_DOCUMENT) {
                        typeImageView.setImageResource(R.drawable.ic_action_doc_green);
                    }
                }

                boolean isBookmarked = DatabaseHelper.getInstance(getActivity()).isBookmarked(menu.getID());
                if (isBookmarked) {
                    favouritedImageView.setVisibility(View.VISIBLE);
                }
            }

            ImageView lockedImageView = (ImageView) view.findViewById(R.id.item_menu_locked);
            boolean isPermitted = DatabaseHelper.getInstance(getActivity()).isPermitted(menu.getID());
            if (!isPermitted) {
                lockedImageView.setVisibility(View.VISIBLE);
            }

            return view;
        }

    };


}

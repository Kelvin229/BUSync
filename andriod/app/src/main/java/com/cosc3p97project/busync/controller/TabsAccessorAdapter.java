package com.cosc3p97project.busync.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cosc3p97project.busync.view.fragment.ChatsFragment;
import com.cosc3p97project.busync.view.fragment.ContactsFragment;
import com.cosc3p97project.busync.view.fragment.GroupsFragment;
import com.cosc3p97project.busync.view.fragment.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0 -> {
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            }
            case 1 -> {
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            }
            case 2 -> {
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            }
            case 3 -> {
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position){
        return switch (position) {
            case 0 -> "Chats";
            case 1 -> "Groups";
            case 2 -> "Contacts";
            case 3 -> "Requests";
            default -> null;
        };
    }
}

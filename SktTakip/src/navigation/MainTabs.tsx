import React from 'react';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import {StyleSheet, Text} from 'react-native';
import {useApp} from '../context/AppContext';
import {useT} from '../i18n';
import {COLORS} from '../theme';
import {HomeScreen} from '../screens/HomeScreen';
import {ProductsScreen} from '../screens/ProductsScreen';
import {NotesScreen} from '../screens/NotesScreen';
import {StatisticsScreen} from '../screens/StatisticsScreen';
import {ProfileScreen} from '../screens/ProfileScreen';
import type {MainTabsParamList} from './types';

const Tab = createBottomTabNavigator<MainTabsParamList>();

const tabIconStyles = StyleSheet.create({
  icon: {fontSize: 20},
  iconFocused: {fontSize: 20, opacity: 1},
  iconUnfocused: {fontSize: 20, opacity: 0.6},
});

const TabIcon: React.FC<{icon: string; focused: boolean}> = ({icon, focused}) => (
  <Text style={focused ? tabIconStyles.iconFocused : tabIconStyles.iconUnfocused}>{icon}</Text>
);

const TAB_ICONS: Record<keyof MainTabsParamList, string> = {
  Home: '🏠',
  Products: '📦',
  Notes: '📝',
  Statistics: '📊',
  Profile: '👤',
};

export const MainTabs: React.FC = () => {
  const {locale} = useApp();
  const t = useT(locale);

  return (
    <Tab.Navigator
      screenOptions={({route}) => ({
        headerShown: false,
        tabBarActiveTintColor: COLORS.primary,
        tabBarInactiveTintColor: COLORS.textLight,
        tabBarLabel: undefined,
        tabBarIcon: ({focused}) => (
          <TabIcon icon={TAB_ICONS[route.name]} focused={focused} />
        ),
        tabBarStyle: {
          backgroundColor: COLORS.surface,
          borderTopColor: COLORS.border,
        },
      })}>
      <Tab.Screen name="Home" component={HomeScreen} options={{tabBarLabel: t('tabHome')}} />
      <Tab.Screen name="Products" component={ProductsScreen} options={{tabBarLabel: t('tabProducts')}} />
      <Tab.Screen name="Notes" component={NotesScreen} options={{tabBarLabel: t('tabNotes')}} />
      <Tab.Screen name="Statistics" component={StatisticsScreen} options={{tabBarLabel: t('tabStatistics')}} />
      <Tab.Screen name="Profile" component={ProfileScreen} options={{tabBarLabel: t('tabProfile')}} />
    </Tab.Navigator>
  );
};

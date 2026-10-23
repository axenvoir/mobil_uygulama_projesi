import React from 'react';
import { View, TouchableOpacity, StyleSheet, Text } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';
import { useTranslation } from '../hooks/useTranslation';
import { SHADOWS } from '../theme';

export default function CustomTabBar({ state, descriptors, navigation }) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const responsive = useResponsive();
  const { isTablet, scale, icon, fontSize } = responsive;

  const height = scale(isTablet ? 85 : 75);
  const iconSize = icon(isTablet ? 26 : 23);
  const plusSize = scale(isTablet ? 70 : 60);

  if (state.routes[state.index]?.name === 'AddProduct') return null;

  
  const totalHeight = height + insets.bottom;

  return (
    <View style={[styles.outerContainer, { backgroundColor: colors.tabBar, height: totalHeight, paddingBottom: insets.bottom }, SHADOWS.heavy]}>
      <View style={styles.tabContainer}>
        {state.routes.map((route, index) => {
          const isFocused = state.index === index;

          if (route.name === 'AddProduct') {
            return (
              <TouchableOpacity
                key={index}
                activeOpacity={0.8}
                accessibilityRole="button"
                accessibilityLabel="Add product"
                accessibilityHint="Tap to add a new product"
                style={[styles.plusButton, {
                  width: plusSize, height: plusSize, borderRadius: plusSize / 2,
                  backgroundColor: colors.primary,
                  borderColor: colors.background,
                  marginTop: -plusSize / 2 - scale(5),
                }, SHADOWS.medium, elevation: 10]}
                onPress={() => navigation.navigate('AddProduct')}
              >
                <Ionicons name="add" size={icon(isTablet ? 38 : 32)} color={colors.onPrimary} />
              </TouchableOpacity>
            );
          }

          const onPress = () => { if (!isFocused) navigation.navigate(route.name); };

          let iconName;
          let label;
          if (route.name === 'Home') { iconName = isFocused ? 'home' : 'home-outline'; label = t('home'); }
          else if (route.name === 'Products') { iconName = isFocused ? 'cube' : 'cube-outline'; label = t('products'); }
          else if (route.name === 'Notes') { iconName = isFocused ? 'document-text' : 'document-text-outline'; label = t('nav_notes'); }
          else if (route.name === 'Notifications') { iconName = isFocused ? 'notifications' : 'notifications-outline'; label = t('nav_notifications'); }

          return (
            <TouchableOpacity
              key={index}
              onPress={onPress}
              activeOpacity={0.7}
              accessibilityRole="tab"
              accessibilityLabel={label}
              accessibilityState={{ selected: isFocused }}
              style={styles.tabItem}
            >
              <View style={styles.iconWrapper}>
                <Ionicons
                  name={iconName}
                  size={iconSize}
                  color={isFocused ? colors.tabBarActive : colors.tabBarInactive}
                />
                {isFocused && (
                  <View style={[styles.activeDot, { backgroundColor: colors.tabBarActive, width: scale(5), height: scale(5), borderRadius: scale(2.5), bottom: -scale(8) }]} />
                )}
              </View>
              <Text
                numberOfLines={1}
                adjustsFontSizeToFit
                style={[styles.tabLabel, {
                  color: isFocused ? colors.tabBarActive : colors.tabBarInactive,
                  fontSize: fontSize(isTablet ? 11 : 9.5),
                  fontWeight: isFocused ? '800' : '600',
                  marginTop: scale(4)
                }]}
              >
                {label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  outerContainer: {
    width: '100%',
    bottom: 0, 
    zIndex: 1000, 
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    overflow: 'hidden',
  },
  tabContainer: {
    flexDirection: 'row',
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-around',
  },
  tabItem: { flex: 1, minHeight: 44, justifyContent: 'center', alignItems: 'center' },
  iconWrapper: { alignItems: 'center', justifyContent: 'center' },
  activeDot: { position: 'absolute' },
  tabLabel: { textAlign: 'center', width: '100%', letterSpacing: 0.2 },
  plusButton: {
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 5,
    zIndex: 2000, 
  },
});
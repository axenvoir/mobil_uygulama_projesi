import React, { useEffect, useState, useMemo } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '../hooks/useTheme';
import { useResponsive } from '../hooks/useResponsive';

export default function LoadingScreen() {
  const { colors } = useTheme();
  const responsive = useResponsive();
  const { icon } = responsive;
  const styles = useMemo(() => createStyles(colors, responsive), [colors, responsive]);

  const [scaleAnim] = useState(() => new Animated.Value(0.85));
  const [opacity] = useState(() => new Animated.Value(0.5));
  const [rotate] = useState(() => new Animated.Value(0));
  const [fadeIn] = useState(() => new Animated.Value(0));

  useEffect(() => {
    Animated.timing(fadeIn, { toValue: 1, duration: 500, useNativeDriver: true }).start();

    Animated.loop(
      Animated.sequence([
        Animated.parallel([
          Animated.timing(scaleAnim, { toValue: 1.15, duration: 700, useNativeDriver: true }),
          Animated.timing(opacity, { toValue: 1, duration: 700, useNativeDriver: true }),
        ]),
        Animated.parallel([
          Animated.timing(scaleAnim, { toValue: 0.85, duration: 700, useNativeDriver: true }),
          Animated.timing(opacity, { toValue: 0.5, duration: 700, useNativeDriver: true }),
        ]),
      ])
    ).start();

    Animated.loop(
      Animated.timing(rotate, { toValue: 1, duration: 3000, useNativeDriver: true })
    ).start();
  }, [fadeIn, opacity, rotate, scaleAnim]);

  const rotateInterpolate = rotate.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '360deg'],
  });

  return (
    <Animated.View style={[styles.container, { backgroundColor: colors.primaryDark, opacity: fadeIn }]}>
      <Animated.View style={[styles.logo, { backgroundColor: colors.primary, transform: [{ scale: scaleAnim }, { rotate: rotateInterpolate }], opacity }]}>
        <Ionicons name="hourglass" size={icon(54)} color={colors.onPrimary} />
      </Animated.View>
      <Text style={[styles.title, { color: colors.white }]}>Expira</Text>
      <Text style={[styles.tagline, { color: colors.textLight }]}>Son kullanma tarihini unutma</Text>
      <Text style={[styles.subtitle, { color: colors.textLight }]}>Yükleniyor...</Text>
      <View style={styles.dotsRow}>
        <Animated.View style={[styles.dot, { backgroundColor: colors.primary, opacity }]} />
        <View style={[styles.dot, { backgroundColor: colors.primary, opacity: 0.5 }]} />
        <View style={[styles.dot, { backgroundColor: colors.primary, opacity: 0.25 }]} />
      </View>
    </Animated.View>
  );
}

const createStyles = (colors, responsive) => {
  const { scale, fontSize } = responsive;
  return StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
    logo: {
      width: scale(100),
      height: scale(100),
      borderRadius: scale(24),
      justifyContent: 'center',
      alignItems: 'center',
      marginBottom: scale(24),
      shadowColor: colors.primary,
      shadowOffset: { width: 0, height: 0 },
      shadowOpacity: 0.6,
      shadowRadius: scale(20),
      elevation: 10,
    },
    title: { fontSize: scale(34), fontWeight: 'bold', letterSpacing: 1, marginBottom: scale(4) },
    tagline: { fontSize: fontSize(13), marginBottom: scale(24), fontStyle: 'italic' },
    subtitle: { fontSize: fontSize(12), marginBottom: scale(24) },
    dotsRow: { flexDirection: 'row' },
    dot: { width: scale(8), height: scale(8), borderRadius: scale(4), marginHorizontal: scale(4) },
  });
};
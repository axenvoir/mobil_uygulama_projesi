import React from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';

interface ErrorBoundaryProps {
  children: React.ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = {hasError: false};

  static getDerivedStateFromError(): ErrorBoundaryState {
    return {hasError: true};
  }

  componentDidCatch(error: unknown, info: unknown): void {
    if (__DEV__) {
      console.error('Expira crashed:', error, info);
    }
  }

  reset = (): void => {
    this.setState({hasError: false});
  };

  render(): React.ReactNode {
    if (this.state.hasError) {
      return (
        <View style={styles.container}>
          <ScrollView contentContainerStyle={styles.content}>
            <Text style={styles.icon}>⚠️</Text>
            <Text style={styles.title}>Expira</Text>
            <Text style={styles.message}>
              An unexpected error occurred. Please restart the app.
            </Text>
          </ScrollView>
        </View>
      );
    }
    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: COLORS.primaryDark},
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: SPACING.xl,
  },
  icon: {fontSize: 56, marginBottom: SPACING.md},
  title: {fontSize: TYPOGRAPHY.h1, fontWeight: '700', color: COLORS.white},
  message: {
    fontSize: TYPOGRAPHY.body,
    color: '#A7C4BD',
    textAlign: 'center',
    marginTop: SPACING.sm,
  },
});

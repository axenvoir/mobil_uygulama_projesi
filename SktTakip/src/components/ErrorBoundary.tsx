import React from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import {captureError} from '../utils/crashLogging';
import {COLORS, SPACING, TYPOGRAPHY} from '../theme';

interface ErrorBoundaryProps {
  children: React.ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  message: string;
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = {hasError: false, message: ''};

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {hasError: true, message: error?.message ?? String(error)};
  }

  componentDidCatch(error: unknown, info: unknown): void {
    captureError(error, 'ErrorBoundary');
    if (__DEV__) {
      console.error('Expira crashed:', error, info);
    }
  }

  reset = (): void => {
    this.setState({hasError: false, message: ''});
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
            {__DEV__ || this.state.message ? (
              <Text style={styles.detail}>{this.state.message}</Text>
            ) : null}
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
  detail: {
    fontSize: TYPOGRAPHY.small,
    color: '#F87171',
    textAlign: 'center',
    marginTop: SPACING.sm,
    paddingHorizontal: SPACING.lg,
  },
});

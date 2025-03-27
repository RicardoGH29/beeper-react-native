import React, {useCallback, useEffect, useState} from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  NativeModules,
  PermissionsAndroid,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';

const {AppsSelectedModule} = NativeModules;

interface AppInfo {
  appName: string;
  packageName: string;
  versionName?: string;
  icon?: string; // Base64 encoded string
}

const InstalledAppsScreen: React.FC = () => {
  const [allApps, setAllApps] = useState<AppInfo[]>([]);
  const [apps, setApps] = useState<AppInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const requestQueryAllPackagesPermission = async (): Promise<boolean> => {
    if (Platform.OS === 'android' && Platform.Version >= 30) {
      try {
        const granted = await PermissionsAndroid.request(
          'android.permission.QUERY_ALL_PACKAGES' as any,
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (error) {
        console.error('Error al solicitar permiso:', error);
        return false;
      }
    }
    return true;
  };

  const loadInstalledApps = useCallback(async () => {
    try {
      if (Platform.OS === 'android') {
        const hasPermission = await requestQueryAllPackagesPermission();
        if (!hasPermission) {
          Alert.alert(
            'Permiso requerido',
            'Se necesita permiso para ver las aplicaciones instaladas.',
          );
          setLoading(false);
          return;
        }

        // Get the response from native module
        const installedApps = await AppsSelectedModule.getAllApps(false);
        console.log('Processed apps array:', installedApps);

        // Update state with the processed array
        setAllApps(
          installedApps
            .map(
              (pkg: {
                appName: any;
                packageName: any;
                versionName: any;
                image: any;
              }) => ({
                appName: pkg.appName || pkg.packageName,
                packageName: pkg.packageName,
                versionName: pkg.versionName,
                icon: pkg.image,
              }),
            )
            .sort((a: {appName: string}, b: {appName: any}) =>
              a.appName.localeCompare(b.appName),
            ),
        );
      } else {
        setAllApps([]);
      }
    } catch (error) {
      console.error('Error al cargar las aplicaciones:', error);
      Alert.alert(
        'Error',
        'No se pudieron cargar las aplicaciones instaladas.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  function selectApp(packageName: string) {
    console.log('Selected app:', packageName);
  }

  useEffect(() => {
    console.log('InstalledAppsScreen');
    loadInstalledApps().then();
  }, [loadInstalledApps]);

  useEffect(() => {
    if (search) {
      setApps(
        allApps.filter(app =>
          app.appName.toLowerCase().includes(search.toLowerCase()),
        ),
      );
    } else {
      setApps(allApps);
    }
  }, [allApps, search]);

  const renderAppItem = ({item}: {item: AppInfo}) => (
    <TouchableOpacity style={styles.appItem} onPress={() => selectApp(item.packageName)}>
      <View style={styles.appIconContainer}>
        {item.icon ? (
          <Image
            source={{uri: `data:image/png;base64,${item.icon}`}}
            style={styles.appIcon}
          />
        ) : (
          <Text style={styles.appIconText}>{item.appName[0]}</Text>
        )}
      </View>
      <View style={styles.appInfo}>
        <Text style={styles.appName}>{item.appName}</Text>
        <Text style={styles.packageName}>{item.packageName}</Text>
        {item.versionName && (
          <Text style={styles.versionName}>Versión: {item.versionName}</Text>
        )}
      </View>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>Cargando aplicaciones...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.searchInput}
        onChangeText={setSearch}
        value={search}
        placeholder="Buscar aplicaciones..."
        placeholderTextColor="#999999"  // Add this line to change placeholder color
      />
      <FlatList
        data={apps}
        renderItem={renderAppItem}
        keyExtractor={item => item.packageName}
        contentContainerStyle={styles.listContainer}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>
              {Platform.OS === 'ios'
                ? 'La funcionalidad de listar aplicaciones instaladas no está disponible en iOS'
                : 'No se encontraron aplicaciones instaladas'}
            </Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666666',
  },
  listContainer: {
    padding: 16,
  },
  appItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 2},
        shadowOpacity: 0.1,
        shadowRadius: 4,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  appIconContainer: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#007AFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  appIconText: {
    color: '#ffffff',
    fontSize: 24,
    fontWeight: 'bold',
  },
  appInfo: {
    flex: 1,
  },
  appName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333333',
    marginBottom: 4,
  },
  packageName: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 2,
  },
  versionName: {
    fontSize: 12,
    color: '#999999',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  emptyText: {
    textAlign: 'center',
    fontSize: 16,
    color: '#666666',
  },
  appIcon: {
    width: 50,
    height: 50,
    borderRadius: 25,
  },
  searchInput: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    margin: 10,
    color: '#000000',
  },
});

export default InstalledAppsScreen;

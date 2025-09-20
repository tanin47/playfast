import type {User} from './models'

// @ts-expect-error defined globally
export const LOGGED_IN_USER: User | null = window.LOGGED_IN_USER

export function getVersionedAsset(path: string): string {
  // @ts-expect-error defined globally
  const versionedAssets = window.VERSIONED_ASSETS;
  if (!versionedAssets) {
    return `/assets/${path}`;
  }

  return `/assets/${versionedAssets[path]}`;
}
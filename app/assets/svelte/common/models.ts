export const PreferredLangs = ['English', 'Thai', 'Japanese', 'German']
export type PreferredLang = typeof PreferredLangs[number]

export interface User {
  id: string
  email: string
  preferredLang: PreferredLang | null
  shouldReceiveNewsletter: boolean
  dummyCounter: number
  createdAt: number
}

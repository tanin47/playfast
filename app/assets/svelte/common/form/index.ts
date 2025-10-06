export class FetchError extends Error {
  messages: string[]

  constructor(messages: string[]) {
    super()
    this.messages = messages
  }
}

const unknownError = new FetchError(['An unknown error occurred. Please contact your administrator.'])

export async function post(url: string, data: object): Promise<object> {
  const resp = await (fetch(url, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
  }))


  if (resp.status >= 200 && resp.status < 300) {
    return await resp.json()
  } else if (resp.status >= 300 && resp.status < 400) {
    console.warn("The ajax request returns 3XX as a response. This is not supported.")
    throw unknownError
  } else if (resp.status >= 400 && resp.status < 500) {
    let json: any
    try {
      json = await resp.json()
    } catch (e) {
      throw unknownError
    }

    if (json.errors) {
      throw new FetchError(json.errors)
    } else {
      console.warn("The response status is 4XX, but json.errors doesn't exist. The server has a bug where it doesn't set json.errors. correctly.")
      throw unknownError
    }
  } else {
    throw unknownError
  }
}

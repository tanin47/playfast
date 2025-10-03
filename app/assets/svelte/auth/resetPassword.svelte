<script lang="ts">
import Button from '../common/_button.svelte'
import {FetchError, post} from "../common/form";
import ErrorPanel from '../common/form/_error_panel.svelte';

export let userId: string
export let secretToken: string

let isLoading = false
let errors: string[] = []
let form = {
  password: ''
}

async function submit(): Promise<void> {
  isLoading = true
  try {
    const _json = await post('/reset-password', {userId, secretToken, password: form.password})
    window.location.href = '/'
  } catch (e) {
    isLoading = false
    errors = (e as FetchError).messages
  }
}
</script>

<div class="hero bg-base-200 min-h-screen">
  <div class="hero-content flex-col justify-center items-center">
    <div class="card bg-base-100 min-w-[400px] w-full max-w-sm shrink-0 shadow-2xl">
      <div class="card-body">
        <div class="flex flex-col gap-4">
          <h1 class="card-title">Set a new password</h1>
          <span class="label">New Password</span>
          <input type="password" class="input w-full" placeholder="New Password" data-test-id="password"
                 bind:value={form.password}/>
          <ErrorPanel {errors}/>
          <Button {isLoading} dataTestId="submit-button" onClick={submit}>Update Password</Button>
        </div>
      </div>
    </div>
  </div>
</div>

<style lang="scss">
</style>

<script lang="ts">
import Button from '../common/_button.svelte'
import {FetchError, invokeOnEnter, post} from "../common/form";
import ErrorPanel from '../common/form/_error_panel.svelte';

let isLoading = false
let isDone = false
let errors: string[] = []
let form = {
  email: ''
}

async function submit(): Promise<void> {
  isLoading = true
  try {
    const _json = await post('/forgot-password', form)
    isDone = true
    isLoading = false
  } catch (e) {
    isLoading = false
    errors = (e as FetchError).messages
  }
}
</script>

<div class="hero bg-base-200 min-h-screen">
  <div class="hero-content flex-col justify-center items-center">
    <div class="card bg-base-100 min-w-[400px] w-full max-w-sm shrink-0 shadow-2xl">
      <div class="card-body" onkeydown={invokeOnEnter(submit)}>
        <div class="flex flex-col gap-4">
          <h1 class="card-title">Forgot Password</h1>
          {#if !isDone}
            <span class="label">Email</span>
            <input type="email" class="input w-full" placeholder="Email" data-test-id="email" bind:value={form.email}/>
            <ErrorPanel {errors}/>
            <Button {isLoading} dataTestId="submit-button" onClick={submit}>Send reset link</Button>
            <div>
              Remembered your password? <a href="/login" class="link link-primary">Login</a>
            </div>
          {:else}
            <span>We've sent you a reset password email.</span>
            <a href="/login" class="link link-primary">Back to Login</a>
          {/if}
        </div>
      </div>
    </div>
  </div>
</div>

<style lang="scss">
</style>
